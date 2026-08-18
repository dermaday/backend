package com.likelion.dermaday.api.treatment.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateTreatmentRequest(
        @NotEmpty List<@Valid TreatmentItemRequest> items
) {
}
