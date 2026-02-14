package com.sakny.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StorageErrorCode implements ErrorCode {

    FILE_TOO_LARGE("STORAGE_001", "File size exceeds maximum limit of 5MB", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE("STORAGE_002", "Invalid file type. Only JPEG, PNG, and WebP images are allowed", HttpStatus.BAD_REQUEST),
    UPLOAD_FAILED("STORAGE_003", "Failed to upload file to storage", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_NOT_FOUND("STORAGE_004", "File not found in storage", HttpStatus.NOT_FOUND),
    DELETE_FAILED("STORAGE_005", "Failed to delete file from storage", HttpStatus.INTERNAL_SERVER_ERROR),
    EMPTY_FILE("STORAGE_006", "File is empty", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}

