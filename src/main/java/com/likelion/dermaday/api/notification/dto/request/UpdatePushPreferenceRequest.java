package com.likelion.dermaday.api.notification.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdatePushPreferenceRequest(
        @NotNull Boolean enabled
) {
}
