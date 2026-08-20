package com.likelion.dermaday.api.notification.event;

import com.likelion.dermaday.api.notification.service.PushNotificationSchedulingService;
import com.likelion.dermaday.api.report.event.ReportCreated;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest
@ActiveProfiles("test")
class PushNotificationEventListenerIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private PushNotificationSchedulingService schedulingService;

    @Test
    void schedulesAsPartOfReportTransactionBeforeCommit() {
        ReportCreated event = reportEvent();

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            eventPublisher.publishEvent(event);
            verifyNoInteractions(schedulingService);
        });

        verify(schedulingService).schedule(event);
    }

    @Test
    void doesNotScheduleWhenReportTransactionRollsBack() {
        clearInvocations(schedulingService);
        ReportCreated event = reportEvent();

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            eventPublisher.publishEvent(event);
            status.setRollbackOnly();
        });

        verifyNoInteractions(schedulingService);
    }

    @Test
    void propagatesCosmeticCancellationFailureBeforeCommit() {
        clearInvocations(schedulingService);
        CosmeticNotificationCancellationRequested event = new CosmeticNotificationCancellationRequested(
                1L,
                2L,
                3L,
                Instant.parse("2026-08-21T00:00:00Z")
        );
        doThrow(new IllegalStateException("cancellation failed"))
                .when(schedulingService)
                .cancelCosmetic(event);

        assertThrows(RuntimeException.class, () ->
                new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                        eventPublisher.publishEvent(event)
                )
        );

        verify(schedulingService).cancelCosmetic(event);
    }

    private ReportCreated reportEvent() {
        return new ReportCreated(
                "rpt-event",
                1L,
                2L,
                Instant.parse("2026-08-21T00:00:00Z"),
                List.of(new ReportCreated.ProductSnapshot(
                        3L,
                        "레티놀 세럼",
                        LocalDate.of(2026, 8, 28)
                ))
        );
    }
}
