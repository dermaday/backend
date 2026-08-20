package com.likelion.dermaday.api.onboarding;

import com.likelion.dermaday.api.cosmetic.domain.CosmeticType;
import com.likelion.dermaday.api.cosmetic.domain.IngredientType;
import com.likelion.dermaday.api.cosmetic.dto.request.CreateCosmeticRequest;
import com.likelion.dermaday.api.cosmetic.dto.request.UpdateCosmeticRequest;
import com.likelion.dermaday.api.cosmetic.dto.response.CosmeticResponse;
import com.likelion.dermaday.api.cosmetic.repository.CosmeticRepository;
import com.likelion.dermaday.api.cosmetic.service.CosmeticService;
import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.api.member.repository.MemberRepository;
import com.likelion.dermaday.api.report.dto.ReportInput;
import com.likelion.dermaday.api.report.service.ReportInputQueryService;
import com.likelion.dermaday.api.skin.domain.SkinType;
import com.likelion.dermaday.api.skin.dto.response.SkinProfileResponse;
import com.likelion.dermaday.api.skin.repository.SkinProfileRepository;
import com.likelion.dermaday.api.skin.service.SkinProfileService;
import com.likelion.dermaday.api.treatment.domain.TreatmentReaction;
import com.likelion.dermaday.api.treatment.domain.TreatmentType;
import com.likelion.dermaday.api.treatment.dto.request.CreateTreatmentRequest;
import com.likelion.dermaday.api.treatment.dto.request.TreatmentItemRequest;
import com.likelion.dermaday.api.treatment.dto.response.TreatmentResponse;
import com.likelion.dermaday.api.treatment.repository.TreatmentRecordRepository;
import com.likelion.dermaday.api.treatment.service.TreatmentService;
import com.likelion.dermaday.common.exception.BadRequestException;
import com.likelion.dermaday.common.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OnboardingDomainIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TreatmentService treatmentService;

    @Autowired
    private TreatmentRecordRepository treatmentRecordRepository;

    @Autowired
    private CosmeticService cosmeticService;

    @Autowired
    private CosmeticRepository cosmeticRepository;

    @Autowired
    private SkinProfileService skinProfileService;

    @Autowired
    private SkinProfileRepository skinProfileRepository;

    @Autowired
    private ReportInputQueryService reportInputQueryService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.createUser("테스트 회원"));
    }

    @Test
    void createsAggregateAndBuildsReportInputWithoutImage() {
        TreatmentResponse treatment = treatmentService.create(member.getId(), treatmentRequest());
        CosmeticResponse cosmetic = cosmeticService.create(member.getId(), new CreateCosmeticRequest(
                treatment.id(),
                "센텔라 스킨",
                CosmeticType.TONER_SKIN,
                Set.of(IngredientType.RETINOL, IngredientType.AHA),
                "assets/cosmetics/centella-skin.png"
        ));
        skinProfileService.upsert(member.getId(), SkinType.COMBINATION);

        ReportInput input = reportInputQueryService.load(member.getId(), treatment.id());

        assertEquals(treatment.id(), input.treatmentRecordId());
        assertEquals(SkinType.COMBINATION, input.skinType());
        assertEquals(2, input.treatments().size());
        assertEquals(1, input.cosmetics().size());
        assertEquals(cosmetic.name(), input.cosmetics().getFirst().name());

        Member other = memberRepository.save(Member.createUser("다른 회원"));
        assertThrows(NotFoundException.class, () -> treatmentService.find(other.getId(), treatment.id()));
        assertThrows(NotFoundException.class, () -> cosmeticService.find(other.getId(), cosmetic.id()));
    }

    @Test
    void rejectsDuplicateTreatmentTypeAndDeletesWholeRecord() {
        CreateTreatmentRequest duplicateRequest = new CreateTreatmentRequest(List.of(
                new TreatmentItemRequest(
                        TreatmentType.ULTHERA,
                        LocalDate.of(2026, 8, 8),
                        TreatmentReaction.COMFORTABLE
                ),
                new TreatmentItemRequest(
                        TreatmentType.ULTHERA,
                        LocalDate.of(2026, 8, 10),
                        TreatmentReaction.IRRITATED
                )
        ));
        assertThrows(BadRequestException.class, () -> treatmentService.create(member.getId(), duplicateRequest));

        TreatmentResponse treatment = treatmentService.create(member.getId(), treatmentRequest());
        CosmeticResponse cosmetic = cosmeticService.create(member.getId(), new CreateCosmeticRequest(
                treatment.id(),
                "아베 폼클렌저",
                CosmeticType.ESSENCE_AMPOULE_SERUM,
                Set.of(IngredientType.GENERAL_COSMETIC),
                "assets/cosmetics/abe-cleanser.png"
        ));

        treatmentService.delete(member.getId(), treatment.id());

        assertFalse(treatmentRecordRepository.existsById(treatment.id()));
        assertFalse(cosmeticRepository.existsById(cosmetic.id()));
    }

    @Test
    void updatesSingleSkinProfileAndCompletesCosmeticCrud() {
        skinProfileService.upsert(member.getId(), SkinType.DRY);
        SkinProfileResponse updatedSkin = skinProfileService.upsert(member.getId(), SkinType.OILY);
        assertEquals(SkinType.OILY, updatedSkin.skinType());
        assertEquals(updatedSkin, skinProfileService.find(member.getId()));
        assertEquals(1, skinProfileRepository.count());

        TreatmentResponse treatment = treatmentService.create(member.getId(), treatmentRequest());
        CosmeticResponse created = cosmeticService.create(member.getId(), new CreateCosmeticRequest(
                treatment.id(),
                "세럼",
                CosmeticType.ESSENCE_AMPOULE_SERUM,
                Set.of(IngredientType.AHA),
                null
        ));

        CosmeticResponse updated = cosmeticService.update(member.getId(), created.id(), new UpdateCosmeticRequest(
                "비타민 크림",
                CosmeticType.LOTION_CREAM,
                Set.of(IngredientType.VITAMIN_C, IngredientType.GENERAL_COSMETIC),
                "assets/cosmetics/vitamin-cream.png"
        ));

        assertEquals(created.id(), updated.id());
        assertEquals(treatment.id(), updated.treatmentRecordId());
        assertEquals("비타민 크림", updated.name());
        assertEquals(CosmeticType.LOTION_CREAM, updated.productType());
        assertEquals(Set.of(IngredientType.VITAMIN_C, IngredientType.GENERAL_COSMETIC), updated.ingredients());
        assertEquals(updated, cosmeticService.find(member.getId(), created.id()));

        cosmeticService.delete(member.getId(), created.id());
        assertFalse(cosmeticRepository.existsById(created.id()));
    }

    @Test
    void rejectsOtherMembersRecordCosmeticAndImageKey() {
        TreatmentResponse treatment = treatmentService.create(member.getId(), treatmentRequest());
        Member other = memberRepository.save(Member.createUser("다른 회원"));

        assertThrows(NotFoundException.class, () -> cosmeticService.findAll(other.getId(), treatment.id()));
        assertThrows(NotFoundException.class, () -> cosmeticService.create(other.getId(), new CreateCosmeticRequest(
                treatment.id(),
                "침범 제품",
                CosmeticType.OIL,
                Set.of(IngredientType.BHA),
                null
        )));
        assertThrows(BadRequestException.class, () -> cosmeticService.create(member.getId(), new CreateCosmeticRequest(
                treatment.id(),
                "잘못된 이미지",
                CosmeticType.OIL,
                Set.of(IngredientType.BHA),
                "members/" + other.getId() + "/images/other.png"
        )));
    }

    @Test
    void ordersRecordsByLatestTreatmentDateAndRequiresSkinForReport() {
        TreatmentResponse older = treatmentService.create(member.getId(), new CreateTreatmentRequest(List.of(
                new TreatmentItemRequest(
                        TreatmentType.IPL,
                        LocalDate.of(2026, 7, 1),
                        TreatmentReaction.COMFORTABLE
                )
        )));
        TreatmentResponse latest = treatmentService.create(member.getId(), new CreateTreatmentRequest(List.of(
                new TreatmentItemRequest(
                        TreatmentType.FRAXEL,
                        LocalDate.of(2026, 8, 15),
                        TreatmentReaction.IRRITATED
                )
        )));

        List<TreatmentResponse> records = treatmentService.findAll(member.getId());

        assertEquals(List.of(latest.id(), older.id()), records.stream().map(TreatmentResponse::id).toList());
        assertThrows(NotFoundException.class, () -> reportInputQueryService.load(member.getId(), latest.id()));
    }

    private CreateTreatmentRequest treatmentRequest() {
        return new CreateTreatmentRequest(List.of(
                new TreatmentItemRequest(
                        TreatmentType.ULTHERA,
                        LocalDate.of(2026, 8, 8),
                        TreatmentReaction.COMFORTABLE
                ),
                new TreatmentItemRequest(
                        TreatmentType.OLIGIO,
                        LocalDate.of(2026, 8, 10),
                        TreatmentReaction.IRRITATED
                )
        ));
    }
}
