package com.likelion.dermaday.api.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterPushInstallationRequest(
        @NotBlank
        @Size(max = 255)
        String fid
) {
}
