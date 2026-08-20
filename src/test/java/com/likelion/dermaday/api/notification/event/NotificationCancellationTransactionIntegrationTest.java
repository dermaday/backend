package com.likelion.dermaday.api.notification.event;

import com.likelion.dermaday.api.cosmetic.domain.CosmeticType;
import com.likelion.dermaday.api.cosmetic.domain.IngredientType;
import com.likelion.dermaday.api.cosmetic.dto.request.CreateCosmeticRequest;
import com.likelion.dermaday.api.cosmetic.dto.response.CosmeticResponse;
import com.likelion.dermaday.api.cosmetic.repository.CosmeticRepository;
import com.likelion.dermaday.api.cosmetic.service.CosmeticService;
import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.api.member.repository.MemberRepository;
import com.likelion.dermaday.api.notification.service.PushNotificationSchedulingService;
import com.likelion.dermaday.api.report.domain.Report;
import com.likelion.dermaday.api.report.repository.ReportRepository;
import com.likelion.dermaday.api.treatment.domain.TreatmentReaction;
import com.likelion.dermaday.api.treatment.domain.TreatmentType;
import com.likelion.dermaday.api.treatment.dto.request.CreateTreatmentRequest;
import com.likelion.dermaday.api.treatment.dto.request.TreatmentItemRequest;
import com.likelion.dermaday.api.treatment.dto.response.TreatmentResponse;
import com.likelion.dermaday.api.treatment.repository.TreatmentRecordRepository;
import com.likelion.dermaday.api.treatment.service.TreatmentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
class NotificationCancellationTransactionIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TreatmentRecordRepository treatmentRecordRepository;

    @Autowired
    private CosmeticRepository cosmeticRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private TreatmentService treatmentService;

    @Autowired
    private CosmeticService cosmeticService;

    @MockitoBean
    private PushNotificationSchedulingService schedulingService;

    private Member member;
    private TreatmentResponse treatment;
    private CosmeticResponse cosmetic;

    @BeforeEach
    void setUp() {
        reportRepository.deleteAll();
        cosmeticRepository.deleteAll();
        treatmentRecordRepository.deleteAll();
        member = memberRepository.save(Member.createUser("알림 취소 원자성 회원"));
        treatment = treatmentService.create(member.getId(), new CreateTreatmentRequest(List.of(
                new TreatmentItemRequest(
                        TreatmentType.FRAXEL,
                        LocalDate.of(2026, 8, 20),
                        TreatmentReaction.COMFORTABLE
                )
        )));
        cosmetic = cosmeticService.create(member.getId(), new CreateCosmeticRequest(
                treatment.id(),
                "삭제 정합성 제품",
                CosmeticType.ESSENCE_AMPOULE_SERUM,
                Set.of(IngredientType.RETINOL),
                null
        ));
    }

    @AfterEach
    void tearDown() {
        reportRepository.deleteAll();
        cosmeticRepository.deleteAll();
        treatmentRecordRepository.deleteAll();
        memberRepository.deleteById(member.getId());
    }

    @Test
    void rollsBackCosmeticDeletionWhenNotificationCancellationFails() {
        doThrow(new IllegalStateException("cancellation failed"))
                .when(schedulingService)
                .cancelCosmetic(any(CosmeticNotificationCancellationRequested.class));

        assertThrows(RuntimeException.class, () -> cosmeticService.delete(member.getId(), cosmetic.id()));

        assertTrue(cosmeticRepository.existsById(cosmetic.id()));
    }

    @Test
    void rollsBackTreatmentAndReportDeletionWhenNotificationCancellationFails() {
        reportRepository.save(Report.create("rpt-delete-rollback", member.getId(), treatment.id(), "{}"));
        doThrow(new IllegalStateException("cancellation failed"))
                .when(schedulingService)
                .cancelTreatment(member.getId(), treatment.id());

        assertThrows(RuntimeException.class, () -> treatmentService.delete(member.getId(), treatment.id()));

        assertTrue(treatmentRecordRepository.existsById(treatment.id()));
        assertTrue(cosmeticRepository.existsById(cosmetic.id()));
        assertTrue(reportRepository.existsById("rpt-delete-rollback"));
    }
}
