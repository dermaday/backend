package com.likelion.dermaday.api.report.service;

import com.likelion.dermaday.api.cosmetic.domain.CosmeticType;
import com.likelion.dermaday.api.cosmetic.domain.IngredientType;
import com.likelion.dermaday.api.report.domain.HeaderStatus;
import com.likelion.dermaday.api.report.domain.ProductStatus;
import com.likelion.dermaday.api.report.domain.Report;
import com.likelion.dermaday.api.report.domain.RoutineStatus;
import com.likelion.dermaday.api.report.dto.ReportInput;
import com.likelion.dermaday.api.report.dto.ReportResponse;
import com.likelion.dermaday.api.report.dto.RoutinePreviewResponse;
import com.likelion.dermaday.api.report.event.ReportCreated;
import com.likelion.dermaday.api.report.repository.ReportRepository;
import com.likelion.dermaday.api.skin.domain.SkinType;
import com.likelion.dermaday.api.treatment.domain.TreatmentReaction;
import com.likelion.dermaday.api.treatment.domain.TreatmentRecord;
import com.likelion.dermaday.api.treatment.repository.TreatmentRecordRepository;
import com.likelion.dermaday.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final String DISCLAIMER = "이 리포트는 진단이 아닙니다. 전문가합의·경과일수 기준 참고정보입니다.";

    private record IngredientRule(int unlockDay, List<String> evidenceIds) {
    }

    private static final Map<IngredientType, IngredientRule> RULES = Map.of(
            IngredientType.RETINOL, new IngredientRule(7, List.of("P001")),
            IngredientType.AHA, new IngredientRule(1, List.of("P001")),
            IngredientType.BHA, new IngredientRule(1, List.of("P001")),
            IngredientType.VITAMIN_C, new IngredientRule(0, List.of("P097", "P098"))
    );

    private static final Map<String, ReportResponse.EvidencePaper> EVIDENCE = Map.of(
            "P001", new ReportResponse.EvidencePaper("P001",
                    "International Expert Consensus on Integrated Skincare Active Ingredients for Pretreatment and Posttreatment Use With Medical Aesthetic Procedures to Enhance Skin Benefits",
                    "21명의 피부과 전문가 합의로, 레티노이드는 시술 당일과 단기 회복기(0~7일), AHA/BHA는 시술 당일 사용을 피하도록 평가됐어요.",
                    null, "Bjerring P, Draelos ZD, Fabi SG, et al.",
                    "J Cosmet Dermatol. 2026;25(5):e70880. doi:10.1111/jocd.70880",
                    "https://pmc.ncbi.nlm.nih.gov/articles/PMC13145317/"),
            "P002", new ReportResponse.EvidencePaper("P002",
                    "Concomitant Use of Dermo-Cosmetic Skin Care in Aesthetic Procedures: Systematic Review with Expert Panel Recommendations",
                    "104편의 문헌 고찰과 전문가 패널 권고로, 상처 회복기에는 성분과 무관하게 활성 성분을 보류하고 기본 보습·상처관리에 집중하도록 권고해요.",
                    null, "Nikolis A, Nestor MS, Czuwara J, et al.",
                    "Clin Cosmet Investig Dermatol. 2026;19:577128. doi:10.2147/CCID.S577128",
                    "https://www.dovepress.com/concomitant-use-of-dermo-cosmetic-skin-care-in-aesthetic-procedures-sy-peer-reviewed-fulltext-article-CCID"),
            "P073", new ReportResponse.EvidencePaper("P073",
                    "The Effect of Sequential Topical Application of Dermatologic Medications on Absorption: Clinical Considerations",
                    "12개 연구 분석 결과, 제품을 바르는 순서와 제형이 피부 흡수 및 장벽 침투에 직접적인 영향을 미친다고 확인했어요.",
                    null, "Burstein SE, Maibach HI.",
                    "Skin Pharmacol Physiol. 2026;39(2):84–98. doi:10.1159/000550716",
                    "https://pubmed.ncbi.nlm.nih.gov/41610072/"),
            "P097", new ReportResponse.EvidencePaper("P097",
                    "Safety profile of laser-assisted drug delivery of vitamin C, E, and ferulic acid serum following ablative fractional resurfacing: A retrospective chart review",
                    "절제 CO2 레이저 직후 비타민C·E·페룰산 도포 시 부작용 0건으로 회피기간 없이 안전하게 사용할 수 있음을 확인했어요.",
                    null, "Johnson, Kazemi, Farah.",
                    "J Cosmet Laser Ther. 2021",
                    "https://pubmed.ncbi.nlm.nih.gov/35290149/"),
            "P098", new ReportResponse.EvidencePaper("P098",
                    "Reparative Effects of a Topical Antioxidant Serum Containing Vitamin C, Vitamin E, and Ferulic Acid After Ablative Fractional CO2 Laser Treatment for Atrophic Acne Scars: RCT",
                    "절제 프락셀 레이저 후 비타민C 항산화 세럼 도포 시 Day 7 딱지 탈락률과 홍반·수분이 유의하게 개선되고 이상반응이 없었어요.",
                    null, "Shi, Xu, Zhang.",
                    "J Cosmet Dermatol. 25(1):e70634, 2026. doi:10.1111/jocd.70634",
                    "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC12793811/")
    );

    private static final List<CosmeticType> ROUTINE_ORDER = List.of(
            CosmeticType.TONER_SKIN,
            CosmeticType.ESSENCE_AMPOULE_SERUM,
            CosmeticType.LOTION_CREAM,
            CosmeticType.OIL
    );

    private static final List<CosmeticType> BASIC_ROUTINE_ORDER = List.of(
            CosmeticType.TONER_SKIN,
            CosmeticType.LOTION_CREAM
    );

    private static final Map<CosmeticType, String> DEFAULT_TIPS = Map.of(
            CosmeticType.TONER_SKIN, "가볍게 결 정돈",
            CosmeticType.ESSENCE_AMPOULE_SERUM, "얇게 펴바르기",
            CosmeticType.LOTION_CREAM, "마지막에 수분 밀봉",
            CosmeticType.OIL, "소량으로 영양 보충"
    );

    private static final Map<CosmeticType, List<String>> DEFAULT_TAGS = Map.of(
            CosmeticType.TONER_SKIN, List.of("결 정돈", "수분"),
            CosmeticType.ESSENCE_AMPOULE_SERUM, List.of("영양", "수분"),
            CosmeticType.LOTION_CREAM, List.of("보습", "장벽"),
            CosmeticType.OIL, List.of("영양", "보호")
    );

    private final ReportInputQueryService reportInputQueryService;
    private final TreatmentRecordRepository treatmentRecordRepository;
    private final ReportRepository reportRepository;
    private final ReportLlmClient reportLlmClient;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ReportResponse create(Long memberId, String displayName, Long treatmentRecordId) {
        ReportInput input = reportInputQueryService.load(memberId, resolveRecordId(memberId, treatmentRecordId));
        LocalDate today = LocalDate.now(KST);
        String reportId = "rpt-" + UUID.randomUUID();
        reportRepository.saveAndFlush(Report.create(reportId, memberId, input.treatmentRecordId(),
                objectMapper.writeValueAsString(input)));

        ReportResponse.SkinTypeSection skinType = toSkinTypeSection(input.skinType());
        List<ReportResponse.TreatmentRow> treatments = input.treatments().stream()
                .map(t -> new ReportResponse.TreatmentRow(
                        t.treatmentType().getDisplayName(), t.treatedOn(), t.reaction(), t.reaction().getDisplayName()))
                .toList();

        boolean abnormal = input.treatments().stream()
                .anyMatch(t -> t.reaction() == TreatmentReaction.IRRITATED);
        if (abnormal) {
            return basicCareReport(reportId, today, displayName, skinType, treatments, input);
        }

        LocalDate baseDate = input.treatments().stream()
                .map(ReportInput.Treatment::treatedOn)
                .max(Comparator.naturalOrder())
                .orElse(today);

        List<ReportResponse.ProductCard> cards = input.cosmetics().stream()
                .map(c -> judge(c, baseDate, today))
                .toList();
        List<ReportResponse.ProductCard> usable = cards.stream()
                .filter(c -> c.status() != ProductStatus.LOCKED).toList();
        List<ReportResponse.ProductCard> restricted = cards.stream()
                .filter(c -> c.status() == ProductStatus.LOCKED).toList();
        List<String> evidenceIds = collectEvidenceIds(cards);

        if (!restricted.isEmpty()) {
            long maxDaysLeft = restricted.stream()
                    .mapToLong(c -> c.daysLeft() == null ? 1L : c.daysLeft())
                    .max().orElse(1L);
            ReportResponse response = new ReportResponse(reportId, today,
                    new ReportResponse.Header(displayName, HeaderStatus.COUNTDOWN, "D-" + maxDaysLeft, "모든 화장품 해금까지"),
                    skinType, treatments,
                    new ReportResponse.Products(usable, restricted, null),
                    new ReportResponse.Routine(RoutineStatus.LOCKED,
                            "모든 화장품 해금 후 확인할 수 있어요", "미리 확인하기", null, null, null, null),
                    null, papers(evidenceIds),
                    new ReportResponse.LlmMeta(false, null, List.of(), false), DISCLAIMER);
            publishReportCreated(memberId, input, cards, response);
            return response;
        }

        List<ReportResponse.RoutineStep> steps = routineSteps(input.cosmetics(), ROUTINE_ORDER);
        ReportLlmClient.TipResult tips = reportLlmClient.writeTips(steps, skinType.name());
        List<String> withLayering = new ArrayList<>(evidenceIds);
        if (!withLayering.contains("P073")) {
            withLayering.add("P073");
        }
        ReportResponse response = new ReportResponse(reportId, today,
                new ReportResponse.Header(displayName, HeaderStatus.UNLOCK_DONE, null, "모든 화장품이 해금되었어요"),
                skinType, treatments,
                new ReportResponse.Products(usable, List.of(), "모든 화장품이 해금되었어요"),
                new ReportResponse.Routine(RoutineStatus.READY, null, null,
                        "가진 제품으로 이런 순서를 많이 추천해요",
                        "제품을 바르는 순서가 성분 흡수에 영향을 줄 수 있다는 연구도 있어요.",
                        List.of("P073"), tips.steps()),
                null, papers(withLayering),
                new ReportResponse.LlmMeta(tips.applied(), tips.applied() ? "gpt-5.6-terra" : null,
                        tips.applied() ? List.of("routine.steps[].tip", "routine.steps[].tags") : List.of(),
                        !tips.applied()),
                DISCLAIMER);
        publishReportCreated(memberId, input, cards, response);
        return response;
    }

    public RoutinePreviewResponse previewRoutine(Long memberId, String reportId) {
        Report report = reportRepository.findByIdAndMemberId(reportId, memberId)
                .orElseThrow(() -> new NotFoundException("해당 리포트를 찾을 수 없습니다."));
        ReportInput input = objectMapper.readValue(report.getInputPayload(), ReportInput.class);
        List<ReportResponse.RoutineStep> steps = routineSteps(input.cosmetics(), ROUTINE_ORDER);
        ReportLlmClient.TipResult tips = reportLlmClient.writeTips(steps, input.skinType().getDisplayName());
        return new RoutinePreviewResponse(RoutineStatus.PREVIEW, "해금이 끝나면 이런 루틴을 추천해요", tips.steps());
    }

    private Long resolveRecordId(Long memberId, Long treatmentRecordId) {
        if (treatmentRecordId != null) {
            return treatmentRecordId;
        }
        return treatmentRecordRepository.findAllByMember_IdOrderByCreatedAtDesc(memberId).stream()
                .findFirst()
                .map(TreatmentRecord::getId)
                .orElseThrow(() -> new NotFoundException("등록된 시술 기록이 없습니다."));
    }

    private ReportResponse.ProductCard judge(ReportInput.Cosmetic cosmetic, LocalDate baseDate, LocalDate today) {
        Integer maxUnlockDay = null;
        List<String> evidenceIds = new ArrayList<>();
        for (IngredientType ingredient : cosmetic.ingredients()) {
            IngredientRule rule = RULES.get(ingredient);
            if (rule == null) {
                continue;
            }
            if (maxUnlockDay == null || rule.unlockDay() > maxUnlockDay) {
                maxUnlockDay = rule.unlockDay();
            }
            rule.evidenceIds().stream().filter(id -> !evidenceIds.contains(id)).forEach(evidenceIds::add);
        }

        String pill = categoryPill(cosmetic);
        if (maxUnlockDay == null) {
            return new ReportResponse.ProductCard(cosmetic.name(), pill, ProductStatus.USABLE_NO_LIMIT,
                    null, null, null, "일반 화장품이라 제한 없이 사용 가능해요.", null, List.of());
        }

        String evidenceTitleEn = evidenceTitle(evidenceIds);
        LocalDate unlockDate = baseDate.plusDays(maxUnlockDay);
        long elapsed = DAYS.between(baseDate, today);
        boolean retinol = cosmetic.ingredients().contains(IngredientType.RETINOL);

        if (maxUnlockDay == 0) {
            return new ReportResponse.ProductCard(cosmetic.name(), pill, ProductStatus.USABLE,
                    null, unlockDate, 0L,
                    "회피기간 없이 바로 사용 가능해요. 절제레이저 직후 도포해도 안전하다는 임상연구가 있어요.",
                    evidenceTitleEn, evidenceIds);
        }
        if (today.isBefore(unlockDate)) {
            long daysLeft = DAYS.between(today, unlockDate);
            String line = retinol
                    ? "시술일부터 " + maxUnlockDay + "일간 회피하도록 전문가 합의가 있어요. 지금은 D+" + elapsed + "이라 " + daysLeft + "일 더 남았어요."
                    : "시술 당일만 회피하면 돼요. 내일부터 사용 가능해요.";
            return new ReportResponse.ProductCard(cosmetic.name(), pill, ProductStatus.LOCKED,
                    "D-" + daysLeft, unlockDate, daysLeft, line, evidenceTitleEn, evidenceIds);
        }
        String line = retinol
                ? "회피기간이 지나 지금부터 사용 가능해요."
                : "시술 당일만 회피하면 돼요. 이미 지났으니 지금부터 사용 가능해요.";
        return new ReportResponse.ProductCard(cosmetic.name(), pill, ProductStatus.USABLE_NOW,
                null, unlockDate, 0L, line, evidenceTitleEn, evidenceIds);
    }

    private ReportResponse basicCareReport(
            String reportId,
            LocalDate today,
            String displayName,
            ReportResponse.SkinTypeSection skinType,
            List<ReportResponse.TreatmentRow> treatments,
            ReportInput input
    ) {
        List<ReportResponse.ProductCard> usable = new ArrayList<>();
        List<ReportResponse.ProductCard> restricted = new ArrayList<>();
        List<ReportInput.Cosmetic> generalOnly = new ArrayList<>();
        for (ReportInput.Cosmetic cosmetic : input.cosmetics()) {
            boolean active = cosmetic.ingredients().stream()
                    .anyMatch(i -> i != IngredientType.GENERAL_COSMETIC);
            if (active) {
                restricted.add(new ReportResponse.ProductCard(cosmetic.name(), categoryPill(cosmetic),
                        ProductStatus.LOCKED, null, null, null,
                        "지금은 상처가 회복 중이라 성분 종류와 상관없이 활성 성분 제품은 전부 보류하는 게 안전해요.",
                        evidenceTitle(List.of("P002")), List.of("P002")));
            } else {
                generalOnly.add(cosmetic);
                usable.add(new ReportResponse.ProductCard(cosmetic.name(), categoryPill(cosmetic),
                        ProductStatus.USABLE_NO_LIMIT, null, null, null,
                        "일반 화장품이라 제한 없이 사용 가능해요.", null, List.of()));
            }
        }
        return new ReportResponse(reportId, today,
                new ReportResponse.Header(displayName, HeaderStatus.BASIC_CARE, null, "기본 관리 모드"),
                skinType, treatments,
                new ReportResponse.Products(usable, restricted, null),
                new ReportResponse.Routine(RoutineStatus.BASIC, null, null,
                        "자극 없는 세안·보습 위주로만 관리해 주세요.", null, null,
                        routineSteps(generalOnly, BASIC_ROUTINE_ORDER)),
                new ReportResponse.BasicCareAlert("지금은 기본관리만 가능해요!",
                        "지금은 상처가 회복 중이라 성분 종류와 상관없이 활성 성분 제품은 전부 보류하는 게 안전해요. "
                                + "자극 없는 세안·보습 위주로만 관리하고, 진물이나 심한 통증이 있으면 시술기관에 바로 연락하세요.",
                        List.of("레티놀", "AHA", "BHA", "비타민C"), List.of("P002")),
                papers(List.of("P002")),
                new ReportResponse.LlmMeta(false, null, List.of(), false), DISCLAIMER);
    }

    private List<ReportResponse.RoutineStep> routineSteps(List<ReportInput.Cosmetic> cosmetics, List<CosmeticType> order) {
        List<ReportResponse.RoutineStep> steps = new ArrayList<>();
        int stepOrder = 1;
        for (CosmeticType type : order) {
            for (ReportInput.Cosmetic cosmetic : cosmetics) {
                if (cosmetic.productType() == type) {
                    steps.add(new ReportResponse.RoutineStep(stepOrder++, cosmetic.name(), categoryPill(cosmetic),
                            DEFAULT_TAGS.get(type), DEFAULT_TIPS.get(type)));
                    break;
                }
            }
        }
        return steps;
    }

    private ReportResponse.SkinTypeSection toSkinTypeSection(SkinType skinType) {
        return new ReportResponse.SkinTypeSection(skinType, skinType.getDisplayName(), skinType.getDescription());
    }

    private String categoryPill(ReportInput.Cosmetic cosmetic) {
        String ingredients = cosmetic.ingredients().stream()
                .map(IngredientType::getDisplayName)
                .sorted()
                .reduce((a, b) -> a + "·" + b)
                .orElse(IngredientType.GENERAL_COSMETIC.getDisplayName());
        return ingredients + " | " + cosmetic.productType().getDisplayName();
    }

    private String evidenceTitle(List<String> evidenceIds) {
        if (evidenceIds.isEmpty()) {
            return null;
        }
        ReportResponse.EvidencePaper paper = EVIDENCE.get(evidenceIds.get(0));
        return paper == null ? null : paper.titleEn();
    }

    private List<ReportResponse.EvidencePaper> papers(List<String> ids) {
        return ids.stream().map(EVIDENCE::get).filter(java.util.Objects::nonNull).toList();
    }

    private List<String> collectEvidenceIds(List<ReportResponse.ProductCard> cards) {
        List<String> ids = new ArrayList<>();
        for (ReportResponse.ProductCard card : cards) {
            card.evidenceIds().stream().filter(id -> !ids.contains(id)).forEach(ids::add);
        }
        return ids;
    }

    private void publishReportCreated(
            Long memberId,
            ReportInput input,
            List<ReportResponse.ProductCard> cards,
            ReportResponse response
    ) {
        if (input.cosmetics().size() != cards.size()) {
            throw new IllegalStateException("리포트 화장품과 판정 결과 수가 일치하지 않습니다.");
        }
        List<ReportCreated.ProductSnapshot> products = IntStream.range(0, cards.size())
                .mapToObj(index -> new ReportCreated.ProductSnapshot(
                        input.cosmetics().get(index).id(),
                        input.cosmetics().get(index).name(),
                        cards.get(index).unlockDate()
                ))
                .toList();
        eventPublisher.publishEvent(new ReportCreated(
                response.reportId(),
                memberId,
                input.treatmentRecordId(),
                Instant.now(),
                products
        ));
    }
}
