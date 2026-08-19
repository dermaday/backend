package com.likelion.dermaday.api.report.dto;

import com.likelion.dermaday.api.report.domain.RoutineStatus;

import java.util.List;

public record RoutinePreviewResponse(
        RoutineStatus status,
        String notice,
        List<ReportResponse.RoutineStep> steps
) {
}
