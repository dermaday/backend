package com.likelion.dermaday.api.notification.service;

import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.api.member.repository.MemberRepository;
import com.likelion.dermaday.api.notification.config.PushProperties;
import com.likelion.dermaday.api.notification.domain.PushNotificationJob;
import com.likelion.dermaday.api.notification.domain.PushNotificationStatus;
import com.likelion.dermaday.api.notification.domain.PushNotificationType;
import com.likelion.dermaday.api.notification.event.CosmeticNotificationCancellationRequested;
import com.likelion.dermaday.api.report.event.ReportCreated;
import com.likelion.dermaday.api.notification.repository.PushNotificationJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PushNotificationSchedulingService {

    private final MemberRepository memberRepository;
    private final PushNotificationJobRepository jobRepository;
    private final PushProperties properties;

    @Transactional
    public void schedule(ReportCreated event) {
        Member member = memberRepository.findById(event.memberId())
                .orElseThrow(() -> new IllegalStateException(
                        "Report owner must exist when scheduling notifications. memberId=" + event.memberId()
                ));

        List<ReportCreated.ProductSnapshot> datedProducts = event.products().stream()
                .filter(product -> product.unlockDate() != null)
                .toList();

        for (ReportCreated.ProductSnapshot product : datedProducts) {
            Instant scheduledAt = scheduledAt(product.unlockDate());
            if (scheduledAt.isAfter(event.createdAt())) {
                createProductJobIfAbsent(member, event.treatmentRecordId(), product, scheduledAt);
            }
        }

        datedProducts.stream()
                .map(ReportCreated.ProductSnapshot::unlockDate)
                .max(Comparator.naturalOrder())
                .ifPresent(lastUnlockDate -> scheduleAggregateJobs(member, event, lastUnlockDate));
    }

    @Transactional
    public void cancelCosmetic(CosmeticNotificationCancellationRequested event) {
        jobRepository.findByMember_IdAndTreatmentRecordIdAndCosmeticIdAndType(
                        event.memberId(),
                        event.treatmentRecordId(),
                        event.cosmeticId(),
                        PushNotificationType.PRODUCT_UNLOCK
                )
                .ifPresent(PushNotificationJob::cancel);

        List<PushNotificationJob> remaining = jobRepository
                .findAllByMember_IdAndTreatmentRecordIdAndStatusNot(
                        event.memberId(),
                        event.treatmentRecordId(),
                        PushNotificationStatus.CANCELLED
                )
                .stream()
                .filter(job -> job.getType() == PushNotificationType.PRODUCT_UNLOCK)
                .toList();

        if (remaining.isEmpty()) {
            cancelAggregateJobs(event.memberId(), event.treatmentRecordId());
            return;
        }

        Instant lastUnlockAt = remaining.stream()
                .map(PushNotificationJob::getScheduledAt)
                .max(Comparator.naturalOrder())
                .orElseThrow();
        rescheduleAggregateJobs(event, lastUnlockAt);
    }

    @Transactional
    public void cancelTreatment(Long memberId, Long treatmentRecordId) {
        jobRepository.findAllByMember_IdAndTreatmentRecordIdAndStatus(
                        memberId,
                        treatmentRecordId,
                        PushNotificationStatus.PENDING
                )
                .forEach(PushNotificationJob::cancel);
    }

    private void scheduleAggregateJobs(
            Member member,
            ReportCreated event,
            LocalDate lastUnlockDate
    ) {
        Instant tomorrowNoticeAt = scheduledAt(lastUnlockDate.minusDays(1));
        if (tomorrowNoticeAt.isAfter(event.createdAt())) {
            createAggregateJobIfAbsent(
                    member,
                    event.treatmentRecordId(),
                    PushNotificationType.ALL_UNLOCK_TOMORROW,
                    tomorrowNoticeAt
            );
        }

        Instant allUnlockedAt = scheduledAt(lastUnlockDate);
        if (allUnlockedAt.isAfter(event.createdAt())) {
            createAggregateJobIfAbsent(
                    member,
                    event.treatmentRecordId(),
                    PushNotificationType.ALL_UNLOCKED,
                    allUnlockedAt
            );
        }
    }

    private void createProductJobIfAbsent(
            Member member,
            Long treatmentRecordId,
            ReportCreated.ProductSnapshot product,
            Instant scheduledAt
    ) {
        String dedupKey = dedupKey(member.getId(), treatmentRecordId, PushNotificationType.PRODUCT_UNLOCK, product.cosmeticId());
        if (jobRepository.findByDedupKey(dedupKey).isEmpty()) {
            jobRepository.save(PushNotificationJob.productUnlock(
                    member,
                    treatmentRecordId,
                    product.cosmeticId(),
                    product.productName(),
                    scheduledAt,
                    dedupKey
            ));
        }
    }

    private void createAggregateJobIfAbsent(
            Member member,
            Long treatmentRecordId,
            PushNotificationType type,
            Instant scheduledAt
    ) {
        String dedupKey = dedupKey(member.getId(), treatmentRecordId, type, null);
        if (jobRepository.findByDedupKey(dedupKey).isEmpty()) {
            jobRepository.save(PushNotificationJob.aggregate(member, treatmentRecordId, type, scheduledAt, dedupKey));
        }
    }

    private void rescheduleAggregateJobs(CosmeticNotificationCancellationRequested event, Instant lastUnlockAt) {
        Instant tomorrowNoticeAt = ZonedDateTime.ofInstant(lastUnlockAt, properties.zoneId())
                .minusDays(1)
                .toInstant();
        rescheduleOrCancel(
                event.memberId(),
                event.treatmentRecordId(),
                PushNotificationType.ALL_UNLOCK_TOMORROW,
                tomorrowNoticeAt,
                event.deletedAt()
        );
        rescheduleOrCancel(
                event.memberId(),
                event.treatmentRecordId(),
                PushNotificationType.ALL_UNLOCKED,
                lastUnlockAt,
                event.deletedAt()
        );
    }

    private void rescheduleOrCancel(
            Long memberId,
            Long treatmentRecordId,
            PushNotificationType type,
            Instant scheduledAt,
            Instant changedAt
    ) {
        jobRepository.findByMember_IdAndTreatmentRecordIdAndType(memberId, treatmentRecordId, type)
                .ifPresent(job -> {
                    if (scheduledAt.isAfter(changedAt)) {
                        job.reschedule(scheduledAt);
                    } else {
                        job.cancel();
                    }
                });
    }

    private void cancelAggregateJobs(Long memberId, Long treatmentRecordId) {
        jobRepository.findByMember_IdAndTreatmentRecordIdAndType(
                        memberId,
                        treatmentRecordId,
                        PushNotificationType.ALL_UNLOCK_TOMORROW
                )
                .ifPresent(PushNotificationJob::cancel);
        jobRepository.findByMember_IdAndTreatmentRecordIdAndType(
                        memberId,
                        treatmentRecordId,
                        PushNotificationType.ALL_UNLOCKED
                )
                .ifPresent(PushNotificationJob::cancel);
    }

    private Instant scheduledAt(LocalDate date) {
        return date.atTime(properties.sendTime()).atZone(properties.zoneId()).toInstant();
    }

    private String dedupKey(
            Long memberId,
            Long treatmentRecordId,
            PushNotificationType type,
            Long cosmeticId
    ) {
        return memberId + ":" + treatmentRecordId + ":" + type + (cosmeticId == null ? "" : ":" + cosmeticId);
    }
}
