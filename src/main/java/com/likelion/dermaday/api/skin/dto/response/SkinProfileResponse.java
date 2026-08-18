package com.likelion.dermaday.api.skin.dto.response;

import com.likelion.dermaday.api.skin.domain.SkinProfile;
import com.likelion.dermaday.api.skin.domain.SkinType;

import java.time.LocalDateTime;

public record SkinProfileResponse(
        SkinType skinType,
        String name,
        String description,
        LocalDateTime updatedAt
) {
    public static SkinProfileResponse from(SkinProfile profile) {
        return new SkinProfileResponse(
                profile.getSkinType(),
                profile.getSkinType().getDisplayName(),
                profile.getSkinType().getDescription(),
                profile.getUpdatedAt()
        );
    }
}
