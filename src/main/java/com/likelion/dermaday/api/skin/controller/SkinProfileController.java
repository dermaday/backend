package com.likelion.dermaday.api.skin.controller;

import com.likelion.dermaday.api.auth.principal.MemberPrincipal;
import com.likelion.dermaday.api.skin.dto.request.UpdateSkinProfileRequest;
import com.likelion.dermaday.api.skin.dto.response.SkinProfileResponse;
import com.likelion.dermaday.api.skin.dto.response.SkinTypeOptionResponse;
import com.likelion.dermaday.api.skin.service.SkinProfileService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/skin-profile")
@RequiredArgsConstructor
@Tag(name = "피부 타입", description = "현재 회원 피부 타입 API")
@SecurityRequirement(name = SwaggerConfig.COOKIE_AUTH_SCHEME)
public class SkinProfileController {

    private final SkinProfileService skinProfileService;

    @GetMapping("/options")
    @Operation(summary = "피부 타입 선택지 조회")
    public ResponseEntity<ApiResponse<List<SkinTypeOptionResponse>>> options() {
        return ApiResponse.success(
                SuccessStatus.SKIN_PROFILE_OPTION_LIST_GET_SUCCESS,
                SkinTypeOptionResponse.all()
        );
    }

    @GetMapping
    @Operation(summary = "내 피부 타입 조회")
    public ResponseEntity<ApiResponse<SkinProfileResponse>> find(
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ApiResponse.success(
                SuccessStatus.SKIN_PROFILE_GET_SUCCESS,
                skinProfileService.find(principal.id())
        );
    }

    @PutMapping
    @Operation(summary = "내 피부 타입 저장")
    public ResponseEntity<ApiResponse<SkinProfileResponse>> upsert(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody UpdateSkinProfileRequest request
    ) {
        return ApiResponse.success(
                SuccessStatus.SKIN_PROFILE_UPDATE_SUCCESS,
                skinProfileService.upsert(principal.id(), request.skinType())
        );
    }

    @DeleteMapping
    @Operation(summary = "내 피부 타입 삭제")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        skinProfileService.delete(principal.id());
        return ApiResponse.successOnly(SuccessStatus.SKIN_PROFILE_DELETE_SUCCESS);
    }
}
