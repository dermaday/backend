package com.likelion.dermaday.api.notification.service;

import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.api.member.repository.MemberRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class PushSubscriptionServiceIntegrationTest {

    @Autowired
    private PushSubscriptionService subscriptionService;

    @Autowired
    private PushInstallationRepository installationRepository;

    @Autowired
    private PushNotificationJobRepository jobRepository;

    @Autowired
    private PushNotificationSchedulingService schedulingService;

    @Autowired
    private PushDataDeletionService pushDataDeletionService;

    @Autowired
    private MemberRepository memberRepository;

    private Member first;
    private Member second;

    @BeforeEach
    void setUp() {
        jobRepository.deleteAll();
        installationRepository.deleteAll();
        first = memberRepository.save(Member.createUser("첫 회원"));
        second = memberRepository.save(Member.createUser("둘째 회원"));
    }

    @AfterEach
    void tearDown() {
        jobRepository.deleteAll();
        installationRepository.deleteAll();
        memberRepository.deleteAllByIdInBatch(java.util.List.of(first.getId(), second.getId()));
    }

    @Test
    void persistsPreferenceAndReassignsSameFidToCurrentMember() {
        assertFalse(subscriptionService.getPreference(first.getId()).enabled());

        assertTrue(subscriptionService.updatePreference(first.getId(), true).enabled());
        subscriptionService.register(first.getId(), "fid-1");
        subscriptionService.register(second.getId(), "fid-1");

        assertEquals(second.getId(), installationRepository.findByFid("fid-1").orElseThrow().getMember().getId());
        assertTrue(subscriptionService.findFids(first.getId()).isEmpty());
        assertEquals(java.util.List.of("fid-1"), subscriptionService.findFids(second.getId()));

        subscriptionService.remove(second.getId(), "fid-1");
        assertTrue(installationRepository.findByFid("fid-1").isEmpty());
    }

    @Test
    void deletesPushDataBeforeMemberDeletion() {
        subscriptionService.register(first.getId(), "fid-delete");
        schedulingService.schedule(new ReportCreated(
                "rpt-delete",
                first.getId(),
                100L,
                Instant.parse("2026-08-21T00:00:00Z"),
                List.of(new ReportCreated.ProductSnapshot(
                        11L,
                        "삭제 대상 제품",
                        LocalDate.of(2026, 8, 25)
                ))
        ));

        pushDataDeletionService.deleteAllByMember(first.getId());
        memberRepository.deleteById(first.getId());

        assertTrue(subscriptionService.findFids(first.getId()).isEmpty());
        assertEquals(0, jobRepository.count());
        assertFalse(memberRepository.existsById(first.getId()));
    }
}
