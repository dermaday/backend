package com.likelion.dermaday.api.whs.dto;

import com.likelion.dermaday.api.cosmetic.domain.CosmeticType;
import com.likelion.dermaday.api.cosmetic.domain.IngredientType;
import com.likelion.dermaday.api.skin.domain.SkinType;
import com.likelion.dermaday.api.treatment.domain.TreatmentReaction;
import com.likelion.dermaday.api.treatment.domain.TreatmentType;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record WhsPayload(
        SkinType skinType,
        List<Treatment> treatments,
        List<Cosmetic> cosmetics
) {
    public record Treatment(
            TreatmentType treatmentType,
            LocalDate treatedOn,
            TreatmentReaction reaction
    ) {
    }

    public record Cosmetic(
            String name,
            CosmeticType productType,
            Set<IngredientType> ingredients,
            String imageObjectKey
    ) {
    }
}
