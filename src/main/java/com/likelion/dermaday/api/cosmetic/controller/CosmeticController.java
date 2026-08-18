package com.likelion.dermaday.api.cosmetic.controller;

import com.likelion.dermaday.api.auth.principal.MemberPrincipal;
import com.likelion.dermaday.api.cosmetic.dto.request.CreateCosmeticRequest;
import com.likelion.dermaday.api.cosmetic.dto.request.UpdateCosmeticRequest;
import com.likelion.dermaday.api.cosmetic.dto.response.CosmeticOptionsResponse;
import com.likelion.dermaday.api.cosmetic.dto.response.CosmeticResponse;
import com.likelion.dermaday.api.cosmetic.service.CosmeticService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cosmetics")
@RequiredArgsConstructor
@Tag(name = "화장품", description = "시술 기록에 사용하는 화장품 API")
@SecurityRequirement(name = SwaggerConfig.COOKIE_AUTH_SCHEME)
public class CosmeticController {

    private final CosmeticService cosmeticService;

    @GetMapping("/options")
    @Operation(summary = "화장품 제품 타입과 성분 선택지 조회")
    public ResponseEntity<ApiResponse<CosmeticOptionsResponse>> options() {
        return ApiResponse.success(
                SuccessStatus.COSMETIC_OPTION_LIST_GET_SUCCESS,
                CosmeticOptionsResponse.all()
        );
    }

    @GetMapping
    @Operation(summary = "시술 기록의 화장품 목록 조회")
    public ResponseEntity<ApiResponse<List<CosmeticResponse>>> findAll(
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestParam Long treatmentRecordId
    ) {
        return ApiResponse.success(
                SuccessStatus.COSMETIC_LIST_GET_SUCCESS,
                cosmeticService.findAll(principal.id(), treatmentRecordId)
        );
    }

    @GetMapping("/{cosmeticId}")
    @Operation(summary = "화장품 상세 조회")
    public ResponseEntity<ApiResponse<CosmeticResponse>> find(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long cosmeticId
    ) {
        return ApiResponse.success(
                SuccessStatus.COSMETIC_GET_SUCCESS,
                cosmeticService.find(principal.id(), cosmeticId)
        );
    }

    @PostMapping
    @Operation(summary = "화장품 등록")
    public ResponseEntity<ApiResponse<CosmeticResponse>> create(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody CreateCosmeticRequest request
    ) {
        return ApiResponse.success(
                SuccessStatus.COSMETIC_CREATE_SUCCESS,
                cosmeticService.create(principal.id(), request)
        );
    }

    @PutMapping("/{cosmeticId}")
    @Operation(summary = "화장품 수정")
    public ResponseEntity<ApiResponse<CosmeticResponse>> update(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long cosmeticId,
            @Valid @RequestBody UpdateCosmeticRequest request
    ) {
        return ApiResponse.success(
                SuccessStatus.COSMETIC_UPDATE_SUCCESS,
                cosmeticService.update(principal.id(), cosmeticId, request)
        );
    }

    @DeleteMapping("/{cosmeticId}")
    @Operation(summary = "화장품 삭제")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long cosmeticId
    ) {
        cosmeticService.delete(principal.id(), cosmeticId);
        return ApiResponse.successOnly(SuccessStatus.COSMETIC_DELETE_SUCCESS);
    }
}
