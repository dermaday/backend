package com.likelion.dermaday.api.notification.service;

import com.likelion.dermaday.api.cosmetic.repository.CosmeticRepository;
import com.likelion.dermaday.api.notification.config.PushProperties;
import com.likelion.dermaday.api.notification.domain.PushNotificationJob;
import com.likelion.dermaday.api.notification.domain.PushNotificationStatus;
import com.likelion.dermaday.api.notification.domain.PushNotificationType;
import com.likelion.dermaday.api.notification.repository.PushNotificationJobRepository;
import com.likelion.dermaday.api.treatment.repository.TreatmentRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.push", name = "enabled", havingValue = "true")
public class PushNotificationDispatcher {

    private static final String TITLE = "Dermaday";

    private final PushNotificationJobRepository jobRepository;
    private final PushSubscriptionService subscriptionService;
    private final CosmeticRepository cosmeticRepository;
    private final TreatmentRecordRepository treatmentRecordRepository;
    private final PushNotificationJobStateService jobStateService;
    private final PushGateway pushGateway;
    private final PushProperties properties;
    private final Clock clock;

    @Scheduled(fixedDelayString = "${app.push.poll-interval:PT10S}")
    public void dispatchDueJobs() {
        Instant now = clock.instant();
        jobRepository.findAllByScheduledAtLessThanEqualAndStatusOrderByScheduledAtAscIdAsc(
                        now,
                        PushNotificationStatus.PENDING
                )
                .stream()
                .sorted(Comparator
                        .comparing(PushNotificationJob::getScheduledAt)
                        .thenComparingInt(job -> job.getType().getPriority())
                        .thenComparing(PushNotificationJob::getId))
                .map(PushNotificationJob::getId)
                .forEach(jobId -> dispatch(jobId, now));
    }

    private void dispatch(Long jobId, Instant now) {
        jobStateService.claim(jobId).ifPresent(target -> dispatchClaimed(target, now));
    }

    private void dispatchClaimed(PushNotificationJobStateService.DispatchTarget target, Instant now) {
        if (target.scheduledAt().plus(properties.dispatchGrace()).isBefore(now)) {
            jobStateService.markSkipped(target.jobId());
            return;
        }
        if (!target.pushEnabled() || isDeletedTreatment(target) || isDeletedProduct(target)) {
            jobStateService.markSkipped(target.jobId());
            return;
        }

        List<String> fids = subscriptionService.findFids(target.memberId());
        if (fids.isEmpty()) {
            jobStateService.markSkipped(target.jobId());
            return;
        }

        try {
            PushSendResult result = pushGateway.send(fids, message(target));
            if (!result.invalidFids().isEmpty()) {
                try {
                    subscriptionService.removeInvalidFids(result.invalidFids());
                } catch (RuntimeException exception) {
                    log.warn("Failed to remove invalid FIDs. jobId={}", target.jobId(), exception);
                }
            }
            if (result.acceptedCount() > 0) {
                jobStateService.markSent(target.jobId(), clock.instant());
                log.info("FCM request accepted. jobId={}, acceptedCount={}", target.jobId(), result.acceptedCount());
            } else {
                jobStateService.markFailed(target.jobId());
                log.warn("FCM request accepted no destinations. jobId={}", target.jobId());
            }
        } catch (RuntimeException exception) {
            jobStateService.markFailed(target.jobId());
            log.error("FCM request failed. jobId={}", target.jobId(), exception);
        }
    }

    private boolean isDeletedProduct(PushNotificationJobStateService.DispatchTarget target) {
        return target.type() == PushNotificationType.PRODUCT_UNLOCK
                && !cosmeticRepository.findByIdAndTreatmentRecord_Member_Id(target.cosmeticId(), target.memberId()).isPresent();
    }

    private boolean isDeletedTreatment(PushNotificationJobStateService.DispatchTarget target) {
        return !treatmentRecordRepository.existsByIdAndMember_Id(target.treatmentRecordId(), target.memberId());
    }

    private PushMessage message(PushNotificationJobStateService.DispatchTarget target) {
        String body = switch (target.type()) {
            case PRODUCT_UNLOCK -> target.productName() + ", 오늘부터 다시 사용 가능해요.";
            case ALL_UNLOCK_TOMORROW -> "내일이면 모든 제품을 다시 쓸 수 있어요.";
            case ALL_UNLOCKED -> "이제 다 발라도 돼요. 루틴을 확인해보세요.";
        };
        return new PushMessage(TITLE, body, properties.homeUrl(), target.type());
    }
}
