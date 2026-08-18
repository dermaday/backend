package com.likelion.dermaday.api.whs.dto.response;

import com.likelion.dermaday.api.cosmetic.domain.CosmeticType;
import com.likelion.dermaday.api.cosmetic.domain.IngredientType;
import com.likelion.dermaday.api.skin.domain.SkinType;
import com.likelion.dermaday.api.treatment.domain.TreatmentReaction;
import com.likelion.dermaday.api.treatment.domain.TreatmentType;
import com.likelion.dermaday.api.whs.dto.WhsPayload;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record WhsResponse(
        String memberName,
        Skin skin,
        List<Treatment> treatments,
        List<Cosmetic> cosmetics
) {
    public static WhsResponse from(String memberName, WhsPayload payload) {
        return new WhsResponse(
                memberName,
                new Skin(
                        payload.skinType(),
                        payload.skinType().getDisplayName(),
                        payload.skinType().getDescription()
                ),
                payload.treatments().stream().map(Treatment::from).toList(),
                payload.cosmetics().stream().map(Cosmetic::from).toList()
        );
    }

    public record Skin(SkinType skinType, String name, String description) {
    }

    public record Treatment(
            TreatmentType treatmentType,
            String treatmentName,
            LocalDate treatedOn,
            TreatmentReaction reaction,
            String reactionName
    ) {
        private static Treatment from(WhsPayload.Treatment treatment) {
            return new Treatment(
                    treatment.treatmentType(),
                    treatment.treatmentType().getDisplayName(),
                    treatment.treatedOn(),
                    treatment.reaction(),
                    treatment.reaction().getDisplayName()
            );
        }
    }

    public record Cosmetic(
            String name,
            CosmeticType productType,
            String productTypeName,
            Set<IngredientType> ingredients,
            String imageObjectKey
    ) {
        private static Cosmetic from(WhsPayload.Cosmetic cosmetic) {
            return new Cosmetic(
                    cosmetic.name(),
                    cosmetic.productType(),
                    cosmetic.productType().getDisplayName(),
                    cosmetic.ingredients(),
                    cosmetic.imageObjectKey()
            );
        }
    }
}
