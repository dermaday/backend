package com.likelion.dermaday.api.cosmetic.dto.response;

import com.likelion.dermaday.api.cosmetic.domain.CosmeticType;
import com.likelion.dermaday.api.cosmetic.domain.IngredientType;

import java.util.Arrays;
import java.util.List;

public record CosmeticOptionsResponse(
        List<ProductTypeOption> productTypes,
        List<IngredientOption> ingredients
) {
    public static CosmeticOptionsResponse all() {
        return new CosmeticOptionsResponse(
                Arrays.stream(CosmeticType.values())
                        .map(type -> new ProductTypeOption(type, type.getDisplayName()))
                        .toList(),
                Arrays.stream(IngredientType.values())
                        .map(type -> new IngredientOption(type, type.getDisplayName()))
                        .toList()
        );
    }

    public record ProductTypeOption(CosmeticType code, String name) {
    }

    public record IngredientOption(IngredientType code, String name) {
    }
}
