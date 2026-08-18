package com.likelion.dermaday.api.treatment.dto.response;

import com.likelion.dermaday.api.treatment.domain.TreatmentCategory;
import com.likelion.dermaday.api.treatment.domain.TreatmentType;

import java.util.Arrays;
import java.util.List;

public record TreatmentOptionResponse(
        TreatmentCategory category,
        String categoryName,
        List<Option> treatments
) {
    public static List<TreatmentOptionResponse> all() {
        return Arrays.stream(TreatmentCategory.values())
                .map(category -> new TreatmentOptionResponse(
                        category,
                        category.getDisplayName(),
                        Arrays.stream(TreatmentType.values())
                                .filter(type -> type.getCategory() == category)
                                .map(Option::from)
                                .toList()
                ))
                .toList();
    }

    public record Option(
            TreatmentType code,
            String name,
            String description
    ) {
        private static Option from(TreatmentType type) {
            return new Option(type, type.getDisplayName(), type.getDescription());
        }
    }
}
