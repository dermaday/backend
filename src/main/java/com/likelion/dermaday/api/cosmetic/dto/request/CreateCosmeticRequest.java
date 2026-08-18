package com.likelion.dermaday.api.cosmetic.dto.request;

import com.likelion.dermaday.api.cosmetic.domain.CosmeticType;
import com.likelion.dermaday.api.cosmetic.domain.IngredientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateCosmeticRequest(
        @NotNull Long treatmentRecordId,
        @NotBlank @Size(max = 100) String name,
        @NotNull CosmeticType productType,
        @NotEmpty Set<@NotNull IngredientType> ingredients,
        @Size(max = 1024) String imageObjectKey
) {
}
