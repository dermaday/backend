package com.likelion.dermaday.api.treatment.dto.request;

import com.likelion.dermaday.api.treatment.domain.TreatmentReaction;
import com.likelion.dermaday.api.treatment.domain.TreatmentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record TreatmentItemRequest(
        @NotNull TreatmentType treatmentType,
        @NotNull @PastOrPresent LocalDate treatedOn,
        @NotNull TreatmentReaction reaction
) {
}
