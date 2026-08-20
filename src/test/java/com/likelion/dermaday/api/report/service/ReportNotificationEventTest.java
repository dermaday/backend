package com.likelion.dermaday.api.report.service;

import com.likelion.dermaday.api.cosmetic.domain.CosmeticType;
import com.likelion.dermaday.api.cosmetic.domain.IngredientType;
import com.likelion.dermaday.api.report.domain.Report;
import com.likelion.dermaday.api.report.dto.ReportInput;
import com.likelion.dermaday.api.report.event.ReportCreated;
import com.likelion.dermaday.api.report.repository.ReportRepository;
import com.likelion.dermaday.api.skin.domain.SkinType;
import com.likelion.dermaday.api.treatment.domain.TreatmentReaction;
import com.likelion.dermaday.api.treatment.domain.TreatmentType;
import com.likelion.dermaday.api.treatment.repository.TreatmentRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportNotificationEventTest {

    @Mock
    private ReportInputQueryService inputQueryService;

    @Mock
    private TreatmentRecordRepository treatmentRecordRepository;

    @Mock
    private ReportLlmClient reportLlmClient;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void publishesImmutableProductSnapshotAfterNormalReport() {
        LocalDate treatedOn = LocalDate.now(ZoneId.of("Asia/Seoul"));
        when(inputQueryService.load(1L, 100L)).thenReturn(input(TreatmentReaction.COMFORTABLE, treatedOn));
        when(objectMapper.writeValueAsString(any(ReportInput.class))).thenReturn("{}");
        ReportService reportService = new ReportService(
                inputQueryService,
                treatmentRecordRepository,
                reportRepository,
                reportLlmClient,
                objectMapper,
                eventPublisher
        );

        reportService.create(1L, "회원", 100L);

        verify(reportRepository).saveAndFlush(any(Report.class));
        ArgumentCaptor<ReportCreated> captor = ArgumentCaptor.forClass(ReportCreated.class);
        verify(eventPublisher).publishEvent(captor.capture());
        ReportCreated event = captor.getValue();
        assertEquals(1L, event.memberId());
        assertEquals(100L, event.treatmentRecordId());
        assertNotNull(event.reportId());
        assertNotNull(event.createdAt());
        assertEquals(11L, event.products().getFirst().cosmeticId());
        assertEquals("레티놀 세럼", event.products().getFirst().productName());
        assertEquals(treatedOn.plusDays(7), event.products().getFirst().unlockDate());
    }

    @Test
    void doesNotScheduleBasicCareReportWithoutUnlockDate() {
        LocalDate treatedOn = LocalDate.now(ZoneId.of("Asia/Seoul"));
        when(inputQueryService.load(1L, 100L)).thenReturn(input(TreatmentReaction.IRRITATED, treatedOn));
        when(objectMapper.writeValueAsString(any(ReportInput.class))).thenReturn("{}");
        ReportService reportService = new ReportService(
                inputQueryService,
                treatmentRecordRepository,
                reportRepository,
                reportLlmClient,
                objectMapper,
                eventPublisher
        );

        reportService.create(1L, "회원", 100L);

        verify(reportRepository).saveAndFlush(any(Report.class));
        verify(eventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    }

    private ReportInput input(TreatmentReaction reaction, LocalDate treatedOn) {
        return new ReportInput(
                100L,
                SkinType.NORMAL,
                List.of(new ReportInput.Treatment(TreatmentType.FRAXEL, treatedOn, reaction)),
                List.of(new ReportInput.Cosmetic(
                        11L,
                        "레티놀 세럼",
                        CosmeticType.ESSENCE_AMPOULE_SERUM,
                        Set.of(IngredientType.RETINOL)
                ))
        );
    }
}
