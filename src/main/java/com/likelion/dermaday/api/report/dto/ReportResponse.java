package com.likelion.dermaday.api.report.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.likelion.dermaday.api.report.domain.HeaderStatus;
import com.likelion.dermaday.api.report.domain.ProductStatus;
import com.likelion.dermaday.api.report.domain.RoutineStatus;
import com.likelion.dermaday.api.skin.domain.SkinType;
import com.likelion.dermaday.api.treatment.domain.TreatmentReaction;

import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReportResponse(
        String reportId,
        LocalDate asOf,
        Header header,
        SkinTypeSection skinType,
        List<TreatmentRow> treatments,
        Products products,
        Routine routine,
        BasicCareAlert basicCareAlert,
        List<EvidencePaper> evidencePapers,
        LlmMeta llm,
        String disclaimer
) {
    public record Header(
            String userName,
            HeaderStatus status,
            String dDayLabel,
            String line
    ) {
    }

    public record SkinTypeSection(
            SkinType code,
            String name,
            String description
    ) {
    }

    public record TreatmentRow(
            String name,
            LocalDate treatedOn,
            TreatmentReaction reaction,
            String reactionName
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ProductCard(
            String name,
            String categoryPill,
            ProductStatus status,
            String dDayLabel,
            LocalDate unlockDate,
            Long daysLeft,
            String line,
            String evidenceTitleEn,
            List<String> evidenceIds
    ) {
    }

    public record Products(
            List<ProductCard> usable,
            List<ProductCard> restricted,
            String allUnlockedLine
    ) {
    }

    public record RoutineStep(
            int order,
            String productName,
            String categoryPill,
            List<String> tags,
            String tip
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Routine(
            RoutineStatus status,
            String lockNotice,
            String cta,
            String notice,
            String referenceNote,
            List<String> evidenceIds,
            List<RoutineStep> steps
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record BasicCareAlert(
            String title,
            String body,
            List<String> riskGroups,
            List<String> evidenceIds
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record EvidencePaper(
            String id,
            String titleEn,
            String summary,
            Integer consensusRate,
            String authors,
            String journal,
            String url
    ) {
    }

    public record LlmMeta(
            boolean used,
            String model,
            List<String> generated,
            boolean fallback
    ) {
    }
}
