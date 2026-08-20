package com.likelion.dermaday.api.notification.controller;

import com.likelion.dermaday.api.auth.principal.MemberPrincipal;
import com.likelion.dermaday.api.notification.dto.request.RegisterPushInstallationRequest;
import com.likelion.dermaday.api.notification.dto.request.RemovePushInstallationRequest;
import com.likelion.dermaday.api.notification.dto.request.UpdatePushPreferenceRequest;
import com.likelion.dermaday.api.notification.dto.response.PushPreferenceResponse;
import com.likelion.dermaday.api.notification.service.PushSubscriptionService;
import com.likelion.dermaday.common.config.swagger.SwaggerConfig;
import com.likelion.dermaday.common.response.ApiResponse;
import com.likelion.dermaday.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications/push")
@RequiredArgsConstructor
@Tag(name = "푸시 알림", description = "푸시 알림 동의 및 브라우저 설치 등록 API")
@SecurityRequirement(name = SwaggerConfig.COOKIE_AUTH_SCHEME)
public class PushNotificationController {

    private final PushSubscriptionService pushSubscriptionService;

    @GetMapping("/preference")
    @Operation(summary = "푸시 알림 동의 조회")
    public ResponseEntity<ApiResponse<PushPreferenceResponse>> getPreference(
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ApiResponse.success(
                SuccessStatus.PUSH_PREFERENCE_GET_SUCCESS,
                pushSubscriptionService.getPreference(principal.id())
        );
    }

    @PatchMapping("/preference")
    @Operation(summary = "푸시 알림 동의 변경")
    public ResponseEntity<ApiResponse<PushPreferenceResponse>> updatePreference(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody UpdatePushPreferenceRequest request
    ) {
        return ApiResponse.success(
                SuccessStatus.PUSH_PREFERENCE_UPDATE_SUCCESS,
                pushSubscriptionService.updatePreference(principal.id(), request.enabled())
        );
    }

    @PutMapping("/installations")
    @Operation(summary = "푸시 수신 브라우저 등록")
    public ResponseEntity<ApiResponse<Void>> registerInstallation(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody RegisterPushInstallationRequest request
    ) {
        pushSubscriptionService.register(principal.id(), request.fid());
        return ApiResponse.successOnly(SuccessStatus.PUSH_INSTALLATION_REGISTER_SUCCESS);
    }

    @DeleteMapping("/installations")
    @Operation(summary = "현재 브라우저의 푸시 수신 등록 삭제")
    public ResponseEntity<ApiResponse<Void>> removeInstallation(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody RemovePushInstallationRequest request
    ) {
        pushSubscriptionService.remove(principal.id(), request.fid());
        return ApiResponse.successOnly(SuccessStatus.PUSH_INSTALLATION_DELETE_SUCCESS);
    }
}
