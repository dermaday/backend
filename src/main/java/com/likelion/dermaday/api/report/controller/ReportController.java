package com.likelion.dermaday.api.report.controller;

import com.likelion.dermaday.api.auth.principal.MemberPrincipal;
import com.likelion.dermaday.api.report.dto.CreateReportRequest;
import com.likelion.dermaday.api.report.dto.ReportResponse;
import com.likelion.dermaday.api.report.dto.RoutinePreviewResponse;
import com.likelion.dermaday.api.report.service.ReportService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "리포트", description = "시술 후 화장품 사용 리포트 API")
@SecurityRequirement(name = SwaggerConfig.COOKIE_AUTH_SCHEME)
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(summary = "리포트 생성", description = "회원의 피부타입·시술 기록·화장품을 조회해 리포트를 생성합니다. treatmentRecordId 미지정 시 최신 시술 기록을 사용합니다.")
    public ResponseEntity<ApiResponse<ReportResponse>> create(
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestBody(required = false) CreateReportRequest request
    ) {
        Long treatmentRecordId = request == null ? null : request.treatmentRecordId();
        ReportResponse data = reportService.create(principal.id(), principal.displayName(), treatmentRecordId);
        return ApiResponse.success(SuccessStatus.REPORT_CREATE_SUCCESS, data);
    }

    @GetMapping("/{reportId}/routine-preview")
    @Operation(summary = "루틴 미리보기", description = "모든 화장품이 해금되었다고 가정한 추천 루틴을 조회합니다.")
    public ResponseEntity<ApiResponse<RoutinePreviewResponse>> previewRoutine(@PathVariable String reportId) {
        return ApiResponse.success(SuccessStatus.REPORT_ROUTINE_PREVIEW_GET_SUCCESS, reportService.previewRoutine(reportId));
    }
}
