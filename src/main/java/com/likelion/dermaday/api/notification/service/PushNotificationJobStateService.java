package com.likelion.dermaday.api.notification.service;

import com.likelion.dermaday.api.notification.domain.PushNotificationJob;
import com.likelion.dermaday.api.notification.domain.PushNotificationStatus;
import com.likelion.dermaday.api.notification.domain.PushNotificationType;
import com.likelion.dermaday.api.notification.repository.PushNotificationJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PushNotificationJobStateService {

    private final PushNotificationJobRepository jobRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<DispatchTarget> claim(Long jobId) {
        int claimed = jobRepository.claim(
                jobId,
                PushNotificationStatus.PENDING,
                PushNotificationStatus.PROCESSING
        );
        if (claimed == 0) {
            return Optional.empty();
        }
        PushNotificationJob job = jobRepository.findById(jobId).orElseThrow();
        return Optional.of(new DispatchTarget(
                job.getId(),
                job.getMember().getId(),
                job.getMember().isPushEnabled(),
                job.getTreatmentRecordId(),
                job.getCosmeticId(),
                job.getType(),
                job.getProductNameSnapshot(),
                job.getScheduledAt()
        ));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSent(Long jobId, Instant acceptedAt) {
        jobRepository.findById(jobId).orElseThrow().markSent(acceptedAt);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSkipped(Long jobId) {
        jobRepository.findById(jobId).orElseThrow().markSkipped();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long jobId) {
        jobRepository.findById(jobId).orElseThrow().markFailed();
    }

    public record DispatchTarget(
            Long jobId,
            Long memberId,
            boolean pushEnabled,
            Long treatmentRecordId,
            Long cosmeticId,
            PushNotificationType type,
            String productName,
            Instant scheduledAt
    ) {
    }
}
