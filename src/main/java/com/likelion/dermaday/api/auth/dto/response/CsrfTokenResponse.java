package com.likelion.dermaday.api.auth.dto.response;

public record CsrfTokenResponse(
        String headerName,
        String token
) {
}
