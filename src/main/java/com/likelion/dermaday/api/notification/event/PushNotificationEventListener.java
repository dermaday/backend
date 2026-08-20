package com.likelion.dermaday.api.notification.event;

import com.likelion.dermaday.api.notification.service.PushNotificationSchedulingService;
import com.likelion.dermaday.api.report.event.ReportCreated;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PushNotificationEventListener {

    private final PushNotificationSchedulingService schedulingService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void scheduleWithReport(ReportCreated event) {
        schedulingService.schedule(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void cancelWithCosmeticDeletion(CosmeticNotificationCancellationRequested event) {
        schedulingService.cancelCosmetic(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void cancelWithTreatmentDeletion(TreatmentNotificationCancellationRequested event) {
        schedulingService.cancelTreatment(event.memberId(), event.treatmentRecordId());
    }
}
