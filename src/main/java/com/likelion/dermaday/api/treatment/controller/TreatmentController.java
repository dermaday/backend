package com.likelion.dermaday.api.treatment.controller;

import com.likelion.dermaday.api.auth.principal.MemberPrincipal;
import com.likelion.dermaday.api.treatment.dto.request.CreateTreatmentRequest;
import com.likelion.dermaday.api.treatment.dto.response.TreatmentOptionResponse;
import com.likelion.dermaday.api.treatment.dto.response.TreatmentResponse;
import com.likelion.dermaday.api.treatment.service.TreatmentService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/treatments")
@RequiredArgsConstructor
@Tag(name = "시술", description = "회원 시술 기록 API")
@SecurityRequirement(name = SwaggerConfig.COOKIE_AUTH_SCHEME)
public class TreatmentController {

    private final TreatmentService treatmentService;

    @GetMapping("/options")
    @Operation(summary = "시술 선택지 조회")
    public ResponseEntity<ApiResponse<List<TreatmentOptionResponse>>> options() {
        return ApiResponse.success(SuccessStatus.TREATMENT_OPTION_LIST_GET_SUCCESS, TreatmentOptionResponse.all());
    }

    @GetMapping
    @Operation(summary = "내 시술 기록 목록 조회")
    public ResponseEntity<ApiResponse<List<TreatmentResponse>>> findAll(
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ApiResponse.success(
                SuccessStatus.TREATMENT_LIST_GET_SUCCESS,
                treatmentService.findAll(principal.id())
        );
    }

    @GetMapping("/{recordId}")
    @Operation(summary = "내 시술 기록 상세 조회")
    public ResponseEntity<ApiResponse<TreatmentResponse>> find(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long recordId
    ) {
        return ApiResponse.success(
                SuccessStatus.TREATMENT_GET_SUCCESS,
                treatmentService.find(principal.id(), recordId)
        );
    }

    @PostMapping
    @Operation(summary = "시술 기록 등록")
    public ResponseEntity<ApiResponse<TreatmentResponse>> create(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody CreateTreatmentRequest request
    ) {
        return ApiResponse.success(
                SuccessStatus.TREATMENT_CREATE_SUCCESS,
                treatmentService.create(principal.id(), request)
        );
    }

    @DeleteMapping("/{recordId}")
    @Operation(summary = "시술 기록 삭제", description = "시술 기록에 속한 화장품과 회원 업로드 이미지도 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long recordId
    ) {
        treatmentService.delete(principal.id(), recordId);
        return ApiResponse.successOnly(SuccessStatus.TREATMENT_DELETE_SUCCESS);
    }
}
