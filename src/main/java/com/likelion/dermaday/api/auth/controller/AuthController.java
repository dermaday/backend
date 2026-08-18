package com.likelion.dermaday.api.auth.controller;

import com.likelion.dermaday.api.auth.cookie.AuthCookieService;
import com.likelion.dermaday.api.auth.dto.response.CsrfTokenResponse;
import com.likelion.dermaday.common.response.ApiResponse;
import com.likelion.dermaday.common.response.SuccessStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthCookieService authCookieService;

    @GetMapping("/csrf")
    public ResponseEntity<ApiResponse<CsrfTokenResponse>> csrf(CsrfToken csrfToken) {
        CsrfTokenResponse data = new CsrfTokenResponse(csrfToken.getHeaderName(), csrfToken.getToken());
        return ApiResponse.success(SuccessStatus.AUTH_SUCCESS, data);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authCookieService.clearAccessToken(response);
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ApiResponse.successOnly(SuccessStatus.LOGOUT_SUCCESS);
    }
}
