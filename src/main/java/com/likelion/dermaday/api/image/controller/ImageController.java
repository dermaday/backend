package com.likelion.dermaday.api.image.controller;

import com.likelion.dermaday.api.auth.principal.MemberPrincipal;
import com.likelion.dermaday.api.image.dto.request.CreatePresignedUploadRequest;
import com.likelion.dermaday.api.image.dto.response.PresignedDownloadResponse;
import com.likelion.dermaday.api.image.dto.response.PresignedUploadResponse;
import com.likelion.dermaday.api.image.service.ImageStorageService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Tag(name = "이미지", description = "S3 Presigned URL API")
@SecurityRequirement(name = SwaggerConfig.COOKIE_AUTH_SCHEME)
public class ImageController {

    private final ImageStorageService imageStorageService;

    @PostMapping("/presigned-upload")
    @Operation(summary = "이미지 업로드 URL 발급")
    public ResponseEntity<ApiResponse<PresignedUploadResponse>> createUploadUrl(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody CreatePresignedUploadRequest request
    ) {
        return ApiResponse.success(
                SuccessStatus.S3_PUT_URL_CREATE_SUCCESS,
                imageStorageService.createUploadUrl(principal.id(), request.contentType(), request.fileSize())
        );
    }

    @GetMapping("/presigned-download")
    @Operation(summary = "이미지 다운로드 URL 발급")
    public ResponseEntity<ApiResponse<PresignedDownloadResponse>> createDownloadUrl(
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestParam String objectKey
    ) {
        return ApiResponse.success(
                SuccessStatus.S3_GET_URL_CREATE_SUCCESS,
                imageStorageService.createDownloadUrl(principal.id(), objectKey)
        );
    }
}
