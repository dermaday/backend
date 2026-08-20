package com.likelion.dermaday.api.notification.service;

import com.likelion.dermaday.api.cosmetic.domain.Cosmetic;
import com.likelion.dermaday.api.cosmetic.repository.CosmeticRepository;
import com.likelion.dermaday.api.notification.config.PushProperties;
import com.likelion.dermaday.api.notification.domain.PushNotificationJob;
import com.likelion.dermaday.api.notification.domain.PushNotificationStatus;
import com.likelion.dermaday.api.notification.domain.PushNotificationType;
import com.likelion.dermaday.api.notification.repository.PushNotificationJobRepository;
import com.likelion.dermaday.api.treatment.repository.TreatmentRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PushNotificationDispatcherTest {

    private static final Instant NOW = Instant.parse("2026-08-22T00:00:10Z");

    @Mock
    private PushNotificationJobRepository jobRepository;

    @Mock
    private PushSubscriptionService subscriptionService;

    @Mock
    private CosmeticRepository cosmeticRepository;

    @Mock
    private TreatmentRecordRepository treatmentRecordRepository;

    @Mock
    private PushNotificationJobStateService jobStateService;

    @Mock
    private PushGateway pushGateway;

    private PushNotificationDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        PushProperties properties = new PushProperties(
                true,
                "dermaday-71877",
                ZoneId.of("Asia/Seoul"),
                LocalTime.of(9, 0),
                URI.create("https://dermaday.me/home"),
                Duration.ofMinutes(1),
                Duration.ofSeconds(10)
        );
        dispatcher = new PushNotificationDispatcher(
                jobRepository,
                subscriptionService,
                cosmeticRepository,
                treatmentRecordRepository,
                jobStateService,
                pushGateway,
                properties,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void sendsSnapshotMessageAndMarksFcmAcceptance() {
        PushNotificationJob job = dueJob(1L);
        PushNotificationJobStateService.DispatchTarget target = new PushNotificationJobStateService.DispatchTarget(
                1L,
                2L,
                true,
                3L,
                4L,
                PushNotificationType.PRODUCT_UNLOCK,
                "레티놀 세럼",
                NOW.minusSeconds(10)
        );
        when(jobRepository.findAllByScheduledAtLessThanEqualAndStatusOrderByScheduledAtAscIdAsc(
                NOW, PushNotificationStatus.PENDING
        )).thenReturn(List.of(job));
        when(jobStateService.claim(1L)).thenReturn(Optional.of(target));
        when(treatmentRecordRepository.existsByIdAndMember_Id(3L, 2L)).thenReturn(true);
        when(cosmeticRepository.findByIdAndTreatmentRecord_Member_Id(4L, 2L))
                .thenReturn(Optional.of(mock(Cosmetic.class)));
        when(subscriptionService.findFids(2L)).thenReturn(List.of("fid-1"));
        when(pushGateway.send(any(), any())).thenReturn(new PushSendResult(1, List.of()));

        dispatcher.dispatchDueJobs();

        ArgumentCaptor<PushMessage> messageCaptor = ArgumentCaptor.forClass(PushMessage.class);
        verify(pushGateway).send(org.mockito.ArgumentMatchers.eq(List.of("fid-1")), messageCaptor.capture());
        assertEquals("Dermaday", messageCaptor.getValue().title());
        assertEquals("레티놀 세럼, 오늘부터 다시 사용 가능해요.", messageCaptor.getValue().body());
        assertEquals(URI.create("https://dermaday.me/home"), messageCaptor.getValue().link());
        verify(jobStateService).markSent(1L, NOW);
    }

    @Test
    void skipsWhenMemberDisabledPushBeforeDueTime() {
        PushNotificationJob job = dueJob(1L);
        PushNotificationJobStateService.DispatchTarget target = new PushNotificationJobStateService.DispatchTarget(
                1L,
                2L,
                false,
                3L,
                null,
                PushNotificationType.ALL_UNLOCKED,
                null,
                NOW
        );
        when(jobRepository.findAllByScheduledAtLessThanEqualAndStatusOrderByScheduledAtAscIdAsc(
                NOW, PushNotificationStatus.PENDING
        )).thenReturn(List.of(job));
        when(jobStateService.claim(1L)).thenReturn(Optional.of(target));

        dispatcher.dispatchDueJobs();

        verify(jobStateService).markSkipped(1L);
        verify(pushGateway, never()).send(any(), any());
    }

    private PushNotificationJob dueJob(Long id) {
        PushNotificationJob job = mock(PushNotificationJob.class);
        when(job.getId()).thenReturn(id);
        return job;
    }
}
