package com.likelion.dermaday.api.skin.dto.request;

import com.likelion.dermaday.api.skin.domain.SkinType;
import jakarta.validation.constraints.NotNull;

public record UpdateSkinProfileRequest(
        @NotNull SkinType skinType
) {
}
