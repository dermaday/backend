package com.likelion.dermaday.api.notification.service;

import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.api.member.repository.MemberRepository;
import com.likelion.dermaday.api.notification.domain.PushNotificationJob;
import com.likelion.dermaday.api.notification.domain.PushNotificationStatus;
import com.likelion.dermaday.api.notification.domain.PushNotificationType;
import com.likelion.dermaday.api.notification.event.CosmeticNotificationCancellationRequested;
import com.likelion.dermaday.api.notification.repository.PushInstallationRepository;
import com.likelion.dermaday.api.notification.repository.PushNotificationJobRepository;
import com.likelion.dermaday.api.report.event.ReportCreated;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class PushNotificationSchedulingServiceIntegrationTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Autowired
    private PushNotificationSchedulingService schedulingService;

    @Autowired
    private PushNotificationJobStateService jobStateService;

    @Autowired
    private PushNotificationJobRepository jobRepository;

    @Autowired
    private PushInstallationRepository installationRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        jobRepository.deleteAll();
        installationRepository.deleteAll();
        member = memberRepository.save(Member.createUser("알림 테스트 회원"));
    }

    @AfterEach
    void tearDown() {
        jobRepository.deleteAll();
        installationRepository.deleteAll();
        memberRepository.deleteById(member.getId());
    }

    @Test
    void storesOnlyFutureSnapshotJobsAndPreventsDuplicates() {
        ReportCreated event = eventAt(
                instant(2026, 8, 21, 8, 0),
                List.of(
                        product(11L, "레티놀 세럼", LocalDate.of(2026, 8, 22)),
                        product(12L, "AHA 토너", LocalDate.of(2026, 8, 23)),
                        product(13L, "일반 크림", null)
                )
        );

        schedulingService.schedule(event);
        schedulingService.schedule(event);

        List<PushNotificationJob> jobs = jobRepository.findAll();
        assertEquals(4, jobs.size());
        assertEquals(2, count(jobs, PushNotificationType.PRODUCT_UNLOCK));
        assertEquals(1, count(jobs, PushNotificationType.ALL_UNLOCK_TOMORROW));
        assertEquals(1, count(jobs, PushNotificationType.ALL_UNLOCKED));
        assertTrue(jobs.stream().allMatch(job -> job.getStatus() == PushNotificationStatus.PENDING));
        assertTrue(jobs.stream().noneMatch(job -> "일반 크림".equals(job.getProductNameSnapshot())));
    }

    @Test
    void skipsTodayWhenReportIsGeneratedAfterSendTime() {
        schedulingService.schedule(eventAt(
                instant(2026, 8, 21, 9, 1),
                List.of(product(11L, "오늘 해금 제품", LocalDate.of(2026, 8, 21)))
        ));

        assertEquals(0, jobRepository.count());
    }

    @Test
    void cancellationMovesAggregateJobsToRemainingLastUnlockDate() {
        schedulingService.schedule(eventAt(
                instant(2026, 8, 21, 8, 0),
                List.of(
                        product(11L, "먼저 해금", LocalDate.of(2026, 8, 22)),
                        product(12L, "마지막 해금", LocalDate.of(2026, 8, 23))
                )
        ));

        schedulingService.cancelCosmetic(new CosmeticNotificationCancellationRequested(
                member.getId(),
                100L,
                12L,
                instant(2026, 8, 21, 8, 30)
        ));

        PushNotificationJob cancelledProduct = jobRepository
                .findByMember_IdAndTreatmentRecordIdAndCosmeticIdAndType(
                        member.getId(), 100L, 12L, PushNotificationType.PRODUCT_UNLOCK
                )
                .orElseThrow();
        PushNotificationJob tomorrow = aggregate(PushNotificationType.ALL_UNLOCK_TOMORROW);
        PushNotificationJob allUnlocked = aggregate(PushNotificationType.ALL_UNLOCKED);

        assertEquals(PushNotificationStatus.CANCELLED, cancelledProduct.getStatus());
        assertEquals(instant(2026, 8, 21, 9, 0), tomorrow.getScheduledAt());
        assertEquals(instant(2026, 8, 22, 9, 0), allUnlocked.getScheduledAt());
    }

    @Test
    void cancellationCalculatesRemainingScheduleIndependentlyOfDeliveryStatus() {
        schedulingService.schedule(eventAt(
                instant(2026, 8, 21, 8, 0),
                List.of(
                        product(11L, "남은 제품", LocalDate.of(2026, 8, 22)),
                        product(12L, "삭제 제품", LocalDate.of(2026, 8, 23))
                )
        ));
        PushNotificationJob remainingProduct = jobRepository
                .findByMember_IdAndTreatmentRecordIdAndCosmeticIdAndType(
                        member.getId(), 100L, 11L, PushNotificationType.PRODUCT_UNLOCK
                )
                .orElseThrow();
        assertTrue(jobStateService.claim(remainingProduct.getId()).isPresent());

        schedulingService.cancelCosmetic(new CosmeticNotificationCancellationRequested(
                member.getId(),
                100L,
                12L,
                instant(2026, 8, 21, 8, 30)
        ));

        assertEquals(instant(2026, 8, 21, 9, 0),
                aggregate(PushNotificationType.ALL_UNLOCK_TOMORROW).getScheduledAt());
        assertEquals(instant(2026, 8, 22, 9, 0),
                aggregate(PushNotificationType.ALL_UNLOCKED).getScheduledAt());
    }

    @Test
    void atomicallyClaimsAndCompletesJob() {
        schedulingService.schedule(eventAt(
                instant(2026, 8, 21, 8, 0),
                List.of(product(11L, "해금 제품", LocalDate.of(2026, 8, 22)))
        ));
        PushNotificationJob productJob = jobRepository.findAll().stream()
                .filter(job -> job.getType() == PushNotificationType.PRODUCT_UNLOCK)
                .findFirst()
                .orElseThrow();

        assertTrue(jobStateService.claim(productJob.getId()).isPresent());
        assertTrue(jobStateService.claim(productJob.getId()).isEmpty());
        jobStateService.markSent(productJob.getId(), instant(2026, 8, 22, 9, 0));

        PushNotificationJob completed = jobRepository.findById(productJob.getId()).orElseThrow();
        assertEquals(PushNotificationStatus.SENT, completed.getStatus());
        assertEquals(instant(2026, 8, 22, 9, 0), completed.getAcceptedAt());
    }

    private PushNotificationJob aggregate(PushNotificationType type) {
        return jobRepository.findByMember_IdAndTreatmentRecordIdAndType(member.getId(), 100L, type)
                .orElseThrow();
    }

    private long count(List<PushNotificationJob> jobs, PushNotificationType type) {
        return jobs.stream().filter(job -> job.getType() == type).count();
    }

    private ReportCreated eventAt(
            Instant generatedAt,
            List<ReportCreated.ProductSnapshot> products
    ) {
        return new ReportCreated("rpt-test", member.getId(), 100L, generatedAt, products);
    }

    private ReportCreated.ProductSnapshot product(
            Long cosmeticId,
            String name,
            LocalDate unlockDate
    ) {
        return new ReportCreated.ProductSnapshot(cosmeticId, name, unlockDate);
    }

    private Instant instant(int year, int month, int day, int hour, int minute) {
        return LocalDate.of(year, month, day).atTime(hour, minute).atZone(KST).toInstant();
    }
}
