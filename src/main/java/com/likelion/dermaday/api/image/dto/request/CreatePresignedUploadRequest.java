package com.likelion.dermaday.api.image.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreatePresignedUploadRequest(
        @NotBlank String contentType,
        @NotNull @Positive @Max(10_485_760) Long fileSize
) {
}
