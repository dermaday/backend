package com.likelion.dermaday.api.notification.event;

import java.time.Instant;
import java.util.Objects;

public record CosmeticNotificationCancellationRequested(
        Long memberId,
        Long treatmentRecordId,
        Long cosmeticId,
        Instant deletedAt
) {
    public CosmeticNotificationCancellationRequested {
        Objects.requireNonNull(memberId, "Member id must not be null");
        Objects.requireNonNull(treatmentRecordId, "Treatment record id must not be null");
        Objects.requireNonNull(cosmeticId, "Cosmetic id must not be null");
        Objects.requireNonNull(deletedAt, "Deleted time must not be null");
    }
}
