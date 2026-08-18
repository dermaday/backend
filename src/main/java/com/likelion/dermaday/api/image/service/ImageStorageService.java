package com.likelion.dermaday.api.image.service;

import com.likelion.dermaday.api.image.config.StorageProperties;
import com.likelion.dermaday.api.image.dto.response.PresignedDownloadResponse;
import com.likelion.dermaday.api.image.dto.response.PresignedUploadResponse;
import com.likelion.dermaday.common.exception.BadRequestException;
import com.likelion.dermaday.common.exception.InternalServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageStorageService {

    public static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024;

    private static final String ASSET_PREFIX = "assets/";
    private static final Map<String, String> ALLOWED_IMAGE_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final StorageProperties properties;

    public PresignedUploadResponse createUploadUrl(Long memberId, String requestedContentType, long fileSize) {
        String contentType = normalizeContentType(requestedContentType);
        validateFileSize(fileSize);
        String objectKey = memberPrefix(memberId)
                + UUID.randomUUID()
                + "."
                + ALLOWED_IMAGE_TYPES.get(contentType);
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .contentType(contentType)
                .contentLength(fileSize)
                .build();
        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(properties.presignedUrlTtl())
                        .putObjectRequest(objectRequest)
                        .build()
        );

        Map<String, String> requiredHeaders = new LinkedHashMap<>();
        presigned.signedHeaders().forEach((name, values) -> {
            if (!"host".equalsIgnoreCase(name) && !"content-length".equalsIgnoreCase(name)) {
                requiredHeaders.put(name, String.join(",", values));
            }
        });
        return new PresignedUploadResponse(
                objectKey,
                presigned.url().toString(),
                presigned.expiration(),
                fileSize,
                requiredHeaders
        );
    }

    public PresignedDownloadResponse createDownloadUrl(Long memberId, String objectKey) {
        validateReadableObject(memberId, objectKey);
        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(properties.presignedUrlTtl())
                        .getObjectRequest(request -> request.bucket(properties.bucket()).key(objectKey))
                        .build()
        );
        return new PresignedDownloadResponse(objectKey, presigned.url().toString(), presigned.expiration());
    }

    public void validateMemberObject(Long memberId, String objectKey) {
        if (objectKey == null || objectKey.isBlank() || !objectKey.startsWith(memberPrefix(memberId))) {
            throw new BadRequestException("유효하지 않은 회원 이미지 경로입니다.");
        }
    }

    public void validateReadableObject(Long memberId, String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new BadRequestException("이미지 경로를 입력해 주세요.");
        }
        if (!objectKey.startsWith(memberPrefix(memberId)) && !objectKey.startsWith(ASSET_PREFIX)) {
            throw new BadRequestException("접근할 수 없는 이미지 경로입니다.");
        }
    }

    public void deleteMemberObject(Long memberId, String objectKey) {
        if (objectKey == null || objectKey.isBlank() || !objectKey.startsWith(memberPrefix(memberId))) {
            return;
        }
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(objectKey)
                    .build());
        } catch (RuntimeException exception) {
            throw new InternalServerException("이미지 삭제에 실패했습니다.");
        }
    }

    private String normalizeContentType(String requestedContentType) {
        if (requestedContentType == null) {
            throw new BadRequestException("이미지 Content-Type을 입력해 주세요.");
        }
        String contentType = requestedContentType.toLowerCase(Locale.ROOT).trim();
        if (!ALLOWED_IMAGE_TYPES.containsKey(contentType)) {
            throw new BadRequestException("JPEG, PNG, WEBP 이미지만 업로드할 수 있습니다.");
        }
        return contentType;
    }

    private void validateFileSize(long fileSize) {
        if (fileSize <= 0 || fileSize > MAX_IMAGE_SIZE_BYTES) {
            throw new BadRequestException("이미지는 10MB 이하만 업로드할 수 있습니다.");
        }
    }

    private String memberPrefix(Long memberId) {
        return "members/" + memberId + "/images/";
    }
}
