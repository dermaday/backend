package com.likelion.dermaday.api.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RemovePushInstallationRequest(
        @NotBlank
        @Size(max = 255)
        String fid
) {
}
