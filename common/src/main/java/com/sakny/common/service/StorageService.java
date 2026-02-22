package com.sakny.common.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for file storage operations.
 */
public interface StorageService {

    /**
     * Upload a profile photo for a user.
     *
     * @param file   the image file to upload
     * @param userId the user's ID
     * @return the public URL of the uploaded file
     */
    String uploadProfilePhoto(MultipartFile file, Long userId);

    /**
     * Delete a file from storage.
     *
     * @param objectKey the object key (path) in the bucket
     */
    void deleteFile(String objectKey);

    /**
     * Get the public URL for a file.
     *
     * @param objectKey the object key (path) in the bucket
     * @return the public URL
     */
    String getFileUrl(String objectKey);

    /**
     * Extract the object key from a full URL.
     *
     * @param fileUrl the full URL of the file
     * @return the object key
     */
    String extractObjectKey(String fileUrl);
}

