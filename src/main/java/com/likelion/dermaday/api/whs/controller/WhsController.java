package com.likelion.dermaday.api.whs.controller;

import com.likelion.dermaday.api.auth.principal.MemberPrincipal;
import com.likelion.dermaday.api.whs.dto.response.WhsImportResponse;
import com.likelion.dermaday.api.whs.dto.response.WhsResponse;
import com.likelion.dermaday.api.whs.service.WhsService;
import com.likelion.dermaday.common.config.swagger.SwaggerConfig;
import com.likelion.dermaday.common.response.ApiResponse;
import com.likelion.dermaday.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/whs")
@RequiredArgsConstructor
@Tag(name = "WHS", description = "고정 WHS mock 조회 및 등록 API")
@SecurityRequirement(name = SwaggerConfig.COOKIE_AUTH_SCHEME)
public class WhsController {

    private final WhsService whsService;

    @GetMapping
    @Operation(summary = "WHS mock 정보 조회")
    public ResponseEntity<ApiResponse<WhsResponse>> preview(
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ApiResponse.success(
                SuccessStatus.WHS_GET_SUCCESS,
                whsService.preview(principal.displayName())
        );
    }

    @PostMapping("/import")
    @Operation(summary = "WHS mock 정보 등록", description = "같은 회원이 다시 요청하면 기존 등록 결과를 반환합니다.")
    public ResponseEntity<ApiResponse<WhsImportResponse>> importData(
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ApiResponse.success(
                SuccessStatus.WHS_IMPORT_SUCCESS,
                whsService.importData(principal.id())
        );
    }
}
