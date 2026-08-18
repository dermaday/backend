package com.likelion.dermaday.api.image.dto.response;

import java.time.Instant;

public record PresignedDownloadResponse(
        String objectKey,
        String downloadUrl,
        Instant expiresAt
) {
}
