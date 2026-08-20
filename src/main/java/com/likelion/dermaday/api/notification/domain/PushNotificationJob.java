package com.likelion.dermaday.api.notification.domain;

import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "push_notification_jobs",
        uniqueConstraints = @UniqueConstraint(name = "uk_push_notification_jobs_dedup_key", columnNames = "dedup_key"),
        indexes = @Index(
                name = "idx_push_notification_jobs_status_scheduled_at",
                columnList = "status, scheduled_at"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushNotificationJob extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "treatment_record_id", nullable = false)
    private Long treatmentRecordId;

    @Column(name = "cosmetic_id")
    private Long cosmeticId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PushNotificationType type;

    @Column(name = "product_name_snapshot", length = 100)
    private String productNameSnapshot;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PushNotificationStatus status;

    @Column(name = "dedup_key", nullable = false, length = 200)
    private String dedupKey;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    private PushNotificationJob(
            Member member,
            Long treatmentRecordId,
            Long cosmeticId,
            PushNotificationType type,
            String productNameSnapshot,
            Instant scheduledAt,
            String dedupKey
    ) {
        this.member = Objects.requireNonNull(member, "Member must not be null");
        this.treatmentRecordId = Objects.requireNonNull(treatmentRecordId, "Treatment record id must not be null");
        this.cosmeticId = cosmeticId;
        this.type = Objects.requireNonNull(type, "Notification type must not be null");
        this.productNameSnapshot = normalizeProductName(productNameSnapshot, type);
        this.scheduledAt = Objects.requireNonNull(scheduledAt, "Scheduled time must not be null");
        this.dedupKey = Objects.requireNonNull(dedupKey, "Dedup key must not be null");
        this.status = PushNotificationStatus.PENDING;
    }

    public static PushNotificationJob productUnlock(
            Member member,
            Long treatmentRecordId,
            Long cosmeticId,
            String productNameSnapshot,
            Instant scheduledAt,
            String dedupKey
    ) {
        return new PushNotificationJob(
                member,
                treatmentRecordId,
                Objects.requireNonNull(cosmeticId, "Cosmetic id must not be null"),
                PushNotificationType.PRODUCT_UNLOCK,
                productNameSnapshot,
                scheduledAt,
                dedupKey
        );
    }

    public static PushNotificationJob aggregate(
            Member member,
            Long treatmentRecordId,
            PushNotificationType type,
            Instant scheduledAt,
            String dedupKey
    ) {
        if (type == PushNotificationType.PRODUCT_UNLOCK) {
            throw new IllegalArgumentException("Product notification requires product data");
        }
        return new PushNotificationJob(member, treatmentRecordId, null, type, null, scheduledAt, dedupKey);
    }

    public void markSent(Instant acceptedAt) {
        requireProcessing();
        this.status = PushNotificationStatus.SENT;
        this.acceptedAt = Objects.requireNonNull(acceptedAt, "Accepted time must not be null");
    }

    public void markSkipped() {
        requireProcessing();
        this.status = PushNotificationStatus.SKIPPED;
    }

    public void markFailed() {
        requireProcessing();
        this.status = PushNotificationStatus.FAILED;
    }

    public void cancel() {
        if (status == PushNotificationStatus.PENDING) {
            this.status = PushNotificationStatus.CANCELLED;
        }
    }

    public void reschedule(Instant scheduledAt) {
        if (status == PushNotificationStatus.PENDING) {
            this.scheduledAt = Objects.requireNonNull(scheduledAt, "Scheduled time must not be null");
        }
    }

    private void requireProcessing() {
        if (status != PushNotificationStatus.PROCESSING) {
            throw new IllegalStateException("Notification job must be processing");
        }
    }

    private static String normalizeProductName(String productName, PushNotificationType type) {
        if (type != PushNotificationType.PRODUCT_UNLOCK) {
            return null;
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        return productName.trim();
    }
}
