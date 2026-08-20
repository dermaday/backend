package com.likelion.dermaday.api.notification.event;

import java.util.Objects;

public record TreatmentNotificationCancellationRequested(Long memberId, Long treatmentRecordId) {
    public TreatmentNotificationCancellationRequested {
        Objects.requireNonNull(memberId, "Member id must not be null");
        Objects.requireNonNull(treatmentRecordId, "Treatment record id must not be null");
    }
}
