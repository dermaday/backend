package com.likelion.dermaday.api.skin.dto.response;

import com.likelion.dermaday.api.skin.domain.SkinType;

import java.util.Arrays;
import java.util.List;

public record SkinTypeOptionResponse(
        SkinType code,
        String name,
        String description
) {
    public static List<SkinTypeOptionResponse> all() {
        return Arrays.stream(SkinType.values())
                .map(type -> new SkinTypeOptionResponse(
                        type,
                        type.getDisplayName(),
                        type.getDescription()
                ))
                .toList();
    }
}
