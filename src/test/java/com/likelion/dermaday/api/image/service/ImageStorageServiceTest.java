package com.likelion.dermaday.api.image.service;

import com.likelion.dermaday.api.image.config.StorageProperties;
import com.likelion.dermaday.api.image.dto.response.PresignedDownloadResponse;
import com.likelion.dermaday.api.image.dto.response.PresignedUploadResponse;
import com.likelion.dermaday.common.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
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

    private S3Presigner presigner() {
        return S3Presigner.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test-access-key", "test-secret-key")
                ))
                .build();
    }
}
