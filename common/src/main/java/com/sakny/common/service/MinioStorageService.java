package com.sakny.common.service;

import com.sakny.common.config.MinioProperties;
import com.sakny.common.exception.BusinessException;
import com.sakny.common.exception.StorageErrorCode;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService implements StorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    private static final String PROFILE_PHOTOS_PREFIX = "profile-photos/";

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public String uploadProfilePhoto(MultipartFile file, Long userId) {
        validateFile(file);

        String objectKey = generateObjectKey(file, userId);

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            String fileUrl = getFileUrl(objectKey);
            log.info("Successfully uploaded profile photo for user {}: {}", userId, fileUrl);
            return fileUrl;

        } catch (Exception e) {
            log.error("Failed to upload profile photo for user {}: {}", userId, e.getMessage(), e);
            throw new BusinessException(StorageErrorCode.UPLOAD_FAILED);
        }
    }

    @Override
    public void deleteFile(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectKey)
                            .build()
            );
            log.info("Successfully deleted file: {}", objectKey);
        } catch (Exception e) {
            log.error("Failed to delete file {}: {}", objectKey, e.getMessage(), e);
            throw new BusinessException(StorageErrorCode.DELETE_FAILED);
        }
    }

    @Override
    public String getFileUrl(String objectKey) {
        return String.format("%s/%s/%s",
                minioProperties.getUrl(),
                minioProperties.getBucketName(),
                objectKey);
    }

    @Override
    public String extractObjectKey(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return null;
        }
        // URL format: http://minio:9000/bucket-name/object-key
        String bucketPrefix = minioProperties.getBucketName() + "/";
        int index = fileUrl.indexOf(bucketPrefix);
        if (index == -1) {
            return null;
        }
        return fileUrl.substring(index + bucketPrefix.length());
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(StorageErrorCode.EMPTY_FILE);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(StorageErrorCode.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(StorageErrorCode.INVALID_FILE_TYPE);
        }
    }

    private String generateObjectKey(MultipartFile file, Long userId) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        } else {
            // Fallback based on content type
            extension = switch (file.getContentType()) {
                case "image/jpeg" -> ".jpg";
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                default -> ".jpg";
            };
        }

        // Generate unique filename: profile-photos/user-{userId}/{uuid}.{ext}
        return String.format("%suser-%d/%s%s",
                PROFILE_PHOTOS_PREFIX,
                userId,
                UUID.randomUUID().toString(),
                extension);
    }
}

