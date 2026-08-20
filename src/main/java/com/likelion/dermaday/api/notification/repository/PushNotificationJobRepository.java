package com.likelion.dermaday.api.notification.repository;

import com.likelion.dermaday.api.notification.domain.PushNotificationJob;
import com.likelion.dermaday.api.notification.domain.PushNotificationStatus;
import com.likelion.dermaday.api.notification.domain.PushNotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PushNotificationJobRepository extends JpaRepository<PushNotificationJob, Long> {

    Optional<PushNotificationJob> findByDedupKey(String dedupKey);

    List<PushNotificationJob> findAllByScheduledAtLessThanEqualAndStatusOrderByScheduledAtAscIdAsc(
            Instant scheduledAt,
            PushNotificationStatus status
    );

    List<PushNotificationJob> findAllByMember_IdAndTreatmentRecordIdAndStatus(
            Long memberId,
            Long treatmentRecordId,
            PushNotificationStatus status
    );

    List<PushNotificationJob> findAllByMember_IdAndTreatmentRecordIdAndStatusNot(
            Long memberId,
            Long treatmentRecordId,
            PushNotificationStatus status
    );

    Optional<PushNotificationJob> findByMember_IdAndTreatmentRecordIdAndCosmeticIdAndType(
            Long memberId,
            Long treatmentRecordId,
            Long cosmeticId,
            PushNotificationType type
    );

    Optional<PushNotificationJob> findByMember_IdAndTreatmentRecordIdAndType(
            Long memberId,
            Long treatmentRecordId,
            PushNotificationType type
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update PushNotificationJob job
               set job.status = :processing
             where job.id = :jobId
               and job.status = :pending
            """)
    int claim(
            @Param("jobId") Long jobId,
            @Param("pending") PushNotificationStatus pending,
            @Param("processing") PushNotificationStatus processing
    );

    void deleteAllByMember_Id(Long memberId);
}
