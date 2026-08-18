package com.likelion.dermaday.api.image.dto.response;

import java.time.Instant;
import java.util.Map;

public record PresignedUploadResponse(
        String objectKey,
        String uploadUrl,
        Instant expiresAt,
        long fileSize,
        Map<String, String> requiredHeaders
) {
}
