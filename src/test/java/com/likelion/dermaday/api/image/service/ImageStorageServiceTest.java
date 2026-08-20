package com.likelion.dermaday.api.image.service;

import com.likelion.dermaday.api.image.config.StorageProperties;
import com.likelion.dermaday.api.image.dto.response.PresignedDownloadResponse;
import com.likelion.dermaday.api.image.dto.response.PresignedUploadResponse;
import com.likelion.dermaday.common.exception.BadRequestException;
import com.likelion.dermaday.common.exception.InternalServerException;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ImageStorageServiceTest {

    private final S3Client s3Client = mock(S3Client.class);
    private final StorageProperties properties = new StorageProperties(
            "ap-northeast-2",
            "dermaday-test",
            Duration.ofMinutes(10)
    );

    @Test
    void createsMemberScopedUploadAndDownloadUrlsWithoutNetworkCall() {
        try (S3Presigner presigner = presigner()) {
            ImageStorageService service = new ImageStorageService(s3Client, presigner, properties);

            PresignedUploadResponse upload = service.createUploadUrl(7L, "image/png", 1_024L);
            PresignedDownloadResponse download = service.createDownloadUrl(7L, upload.objectKey());

            assertTrue(upload.objectKey().startsWith("members/7/images/"));
            assertTrue(upload.objectKey().endsWith(".png"));
            assertEquals(1_024L, upload.fileSize());
            assertTrue(upload.requiredHeaders().entrySet().stream().anyMatch(header ->
                    header.getKey().equalsIgnoreCase("Content-Type")
                            && header.getValue().equals("image/png")
            ));
            assertTrue(upload.uploadUrl().contains("X-Amz-Signature"));
            assertTrue(upload.uploadUrl().contains("content-length"));
            assertTrue(download.downloadUrl().contains("X-Amz-Signature"));
        }
    }

    @Test
    void acceptsExactlyTenMebibytes() {
        try (S3Presigner presigner = presigner()) {
            ImageStorageService service = new ImageStorageService(s3Client, presigner, properties);

            PresignedUploadResponse upload = service.createUploadUrl(
                    1L,
                    "image/webp",
                    ImageStorageService.MAX_IMAGE_SIZE_BYTES
            );

            assertEquals(ImageStorageService.MAX_IMAGE_SIZE_BYTES, upload.fileSize());
            assertTrue(upload.uploadUrl().contains("content-length"));
        }
    }

    @Test
    void rejectsUnsupportedOrOtherMembersImageAndDoesNotDeleteSharedAssets() {
        try (S3Presigner presigner = presigner()) {
            ImageStorageService service = new ImageStorageService(s3Client, presigner, properties);

            assertThrows(BadRequestException.class, () -> service.createUploadUrl(1L, "image/gif", 1_024L));
            assertThrows(BadRequestException.class, () -> service.createUploadUrl(
                    1L,
                    "image/png",
                    ImageStorageService.MAX_IMAGE_SIZE_BYTES + 1
            ));
            assertThrows(BadRequestException.class, () -> service.createUploadUrl(1L, "image/png", 0));
            assertThrows(
                    BadRequestException.class,
                    () -> service.createDownloadUrl(1L, "members/2/images/image.png")
            );

            service.deleteMemberObject(1L, "assets/cosmetics/shared.png");
            verifyNoInteractions(s3Client);
        }
    }

    @Test
    void normalizesContentTypeAndAllowsSharedAssetDownload() {
        try (S3Presigner presigner = presigner()) {
            ImageStorageService service = new ImageStorageService(s3Client, presigner, properties);

            PresignedUploadResponse upload = service.createUploadUrl(3L, "  IMAGE/JPEG  ", 1L);
            PresignedDownloadResponse asset = service.createDownloadUrl(
                    3L,
                    "assets/cosmetics/shared.png"
            );

            assertTrue(upload.objectKey().endsWith(".jpg"));
            assertEquals("image/jpeg", upload.requiredHeaders().entrySet().stream()
                    .filter(header -> header.getKey().equalsIgnoreCase("Content-Type"))
                    .findFirst()
                    .orElseThrow()
                    .getValue());
            assertTrue(asset.downloadUrl().contains("X-Amz-Signature"));
            assertDoesNotThrow(() -> service.validateMemberObject(3L, upload.objectKey()));
            assertThrows(
                    BadRequestException.class,
                    () -> service.validateMemberObject(3L, "assets/cosmetics/shared.png")
            );
        }
    }

    @Test
    void deletesOwnedObjectAndWrapsS3Failure() {
        try (S3Presigner presigner = presigner()) {
            ImageStorageService service = new ImageStorageService(s3Client, presigner, properties);
            String objectKey = "members/4/images/image.png";

            service.deleteMemberObject(4L, objectKey);

            verify(s3Client).deleteObject(argThat((DeleteObjectRequest request) ->
                    request.bucket().equals("dermaday-test") && request.key().equals(objectKey)
            ));

            doThrow(new IllegalStateException("S3 unavailable"))
                    .when(s3Client)
                    .deleteObject(any(DeleteObjectRequest.class));
            assertThrows(
                    InternalServerException.class,
                    () -> service.deleteMemberObject(4L, objectKey)
            );
        }
    }

    @Test
    void rejectsMissingContentTypeAndObjectKey() {
        try (S3Presigner presigner = presigner()) {
            ImageStorageService service = new ImageStorageService(s3Client, presigner, properties);

            assertThrows(BadRequestException.class, () -> service.createUploadUrl(1L, null, 1L));
            assertThrows(BadRequestException.class, () -> service.createDownloadUrl(1L, " "));
            assertThrows(BadRequestException.class, () -> service.validateMemberObject(1L, null));
        }
    }

    private S3Presigner presigner() {
        return S3Presigner.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test-access-key", "test-secret-key")
                ))
                .build();
    }
}
