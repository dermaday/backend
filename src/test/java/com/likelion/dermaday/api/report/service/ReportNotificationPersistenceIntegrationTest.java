package com.likelion.dermaday.api.report.service;

import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.api.member.repository.MemberRepository;
import com.likelion.dermaday.api.notification.repository.PushNotificationJobRepository;
import com.likelion.dermaday.api.report.domain.Report;
import com.likelion.dermaday.api.report.event.ReportCreated;
import com.likelion.dermaday.api.report.repository.ReportRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class ReportNotificationPersistenceIntegrationTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PushNotificationJobRepository jobRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReportDataDeletionService deletionService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Member member;

    @BeforeEach
    void setUp() {
        jobRepository.deleteAll();
        reportRepository.deleteAll();
        member = memberRepository.save(Member.createUser("리포트 원자성 회원"));
    }

    @AfterEach
    void tearDown() {
        jobRepository.deleteAll();
        reportRepository.deleteAll();
        memberRepository.deleteById(member.getId());
    }

    @Test
    void commitsReportAndNotificationSnapshotsTogether() {
        ReportCreated event = reportEvent("rpt-atomic", member.getId(), 100L);

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            reportRepository.save(Report.create("rpt-atomic", member.getId(), 100L, "{}"));
            eventPublisher.publishEvent(event);
            assertEquals(0, jobRepository.count());
        });

        assertTrue(reportRepository.existsById("rpt-atomic"));
        assertEquals(3, jobRepository.count());
    }

    @Test
    void rollsBackReportWhenNotificationSchedulingFails() {
        ReportCreated event = reportEvent("rpt-rollback", Long.MAX_VALUE, 100L);

        assertThrows(RuntimeException.class, () ->
                new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                    reportRepository.save(Report.create("rpt-rollback", Long.MAX_VALUE, 100L, "{}"));
                    eventPublisher.publishEvent(event);
                })
        );

        assertTrue(reportRepository.findById("rpt-rollback").isEmpty());
        assertEquals(0, jobRepository.count());
    }

    @Test
    void preventsDuplicateReportsAndDeletesReportsWithTheirOwnerData() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                reportRepository.saveAndFlush(Report.create("rpt-first", member.getId(), 100L, "{}"))
        );

        assertThrows(DataIntegrityViolationException.class, () ->
                new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                        reportRepository.saveAndFlush(Report.create("rpt-second", member.getId(), 100L, "{}"))
                )
        );

        deletionService.deleteByTreatmentRecord(member.getId(), 100L);
        assertEquals(0, reportRepository.count());

        reportRepository.save(Report.create("rpt-member-delete", member.getId(), 101L, "{}"));
        deletionService.deleteAllByMember(member.getId());
        assertEquals(0, reportRepository.count());
    }

    private ReportCreated reportEvent(String reportId, Long memberId, Long treatmentRecordId) {
        return new ReportCreated(
                reportId,
                memberId,
                treatmentRecordId,
                Instant.parse("2026-08-21T00:00:00Z"),
                List.of(new ReportCreated.ProductSnapshot(
                        11L,
                        "레티놀 세럼",
                        LocalDate.of(2026, 8, 25)
                ))
        );
    }
}
