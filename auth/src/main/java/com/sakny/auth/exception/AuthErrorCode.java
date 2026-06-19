package com.sakny.auth.exception;

import com.sakny.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    USER_NOT_FOUND("AUTH_001", "User not found", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS("AUTH_002", "Email is already taken", HttpStatus.CONFLICT),
    PHONE_ALREADY_EXISTS("AUTH_003", "Phone number is already taken", HttpStatus.CONFLICT),
    OTP_NOT_FOUND("AUTH_004", "No active OTP found for this account", HttpStatus.NOT_FOUND),
    OTP_EXPIRED("AUTH_005", "OTP has expired, please request a new one", HttpStatus.BAD_REQUEST),
    OTP_INVALID("AUTH_006", "Invalid OTP code", HttpStatus.BAD_REQUEST),
    OTP_ALREADY_USED("AUTH_007", "OTP has already been used", HttpStatus.BAD_REQUEST),
    OTP_SEND_FAILED("AUTH_008", "Failed to send OTP, please try again", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_LOCKED("AUTH_009", "Account is temporarily locked due to too many failed attempts", HttpStatus.LOCKED),
    REFRESH_TOKEN_INVALID("AUTH_010", "Invalid refresh token", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED("AUTH_011", "Refresh token has expired, please login again", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_REUSED("AUTH_012", "Token reuse detected, all sessions revoked for security", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS("AUTH_013", "Invalid email or password", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
