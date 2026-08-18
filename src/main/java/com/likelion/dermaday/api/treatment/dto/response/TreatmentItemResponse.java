package com.likelion.dermaday.api.treatment.dto.response;

import com.likelion.dermaday.api.treatment.domain.TreatmentItem;
import com.likelion.dermaday.api.treatment.domain.TreatmentReaction;
import com.likelion.dermaday.api.treatment.domain.TreatmentType;

import java.time.LocalDate;

public record TreatmentItemResponse(
        Long id,
        TreatmentType treatmentType,
        String treatmentName,
        LocalDate treatedOn,
        TreatmentReaction reaction,
        String reactionName
) {
    public static TreatmentItemResponse from(TreatmentItem item) {
        return new TreatmentItemResponse(
                item.getId(),
                item.getTreatmentType(),
                item.getTreatmentType().getDisplayName(),
                item.getTreatedOn(),
                item.getReaction(),
                item.getReaction().getDisplayName()
        );
    }
}
