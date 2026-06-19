package com.sakny.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum VerificationErrorCode implements ErrorCode {

    VERIFICATION_ALREADY_PENDING("VERIF_001", "A verification is already pending for this user", HttpStatus.CONFLICT),
    VERIFICATION_ALREADY_APPROVED("VERIF_002", "This user is already verified", HttpStatus.CONFLICT),
    VERIFICATION_NOT_FOUND("VERIF_003", "No verification submission found for this user", HttpStatus.NOT_FOUND),
    WEBHOOK_REFERENCE_NOT_FOUND("VERIF_004", "Webhook reference not found", HttpStatus.NOT_FOUND),
    VERIFICATION_UPLOAD_FAILED("VERIF_005", "Failed to upload verification document", HttpStatus.INTERNAL_SERVER_ERROR),
    VERIFICATION_EXTERNAL_API_ERROR("VERIF_006", "Failed to communicate with verification provider", HttpStatus.BAD_GATEWAY),
    USER_NOT_FOUND("VERIF_007", "User not found", HttpStatus.NOT_FOUND),
    WEBHOOK_SIGNATURE_INVALID("VERIF_008", "Invalid webhook signature", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
