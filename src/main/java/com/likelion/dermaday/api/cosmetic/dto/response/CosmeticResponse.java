package com.likelion.dermaday.api.cosmetic.dto.response;

import com.likelion.dermaday.api.cosmetic.domain.Cosmetic;
import com.likelion.dermaday.api.cosmetic.domain.CosmeticType;
import com.likelion.dermaday.api.cosmetic.domain.IngredientType;

import java.time.LocalDateTime;
import java.util.Set;

public record CosmeticResponse(
        Long id,
        Long treatmentRecordId,
        String name,
        CosmeticType productType,
        String productTypeName,
        Set<IngredientType> ingredients,
        String imageObjectKey,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CosmeticResponse from(Cosmetic cosmetic) {
        return new CosmeticResponse(
                cosmetic.getId(),
                cosmetic.getTreatmentRecord().getId(),
                cosmetic.getName(),
                cosmetic.getProductType(),
                cosmetic.getProductType().getDisplayName(),
                Set.copyOf(cosmetic.getIngredients()),
                cosmetic.getImageObjectKey(),
                cosmetic.getCreatedAt(),
                cosmetic.getUpdatedAt()
        );
    }
}
