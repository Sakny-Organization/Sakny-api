package com.sakny.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProfileErrorCode implements ErrorCode {

    PROFILE_ALREADY_EXISTS("PROFILE_001", "Profile already exists for this user", HttpStatus.CONFLICT),
    PROFILE_NOT_FOUND("PROFILE_002", "Profile not found", HttpStatus.NOT_FOUND),
    INVALID_BUDGET_RANGE("PROFILE_003", "Budget min must be less than or equal to budget max", HttpStatus.BAD_REQUEST),
    INVALID_GOVERNORATE("PROFILE_004", "Governorate not found", HttpStatus.BAD_REQUEST),
    INVALID_CITY("PROFILE_005", "City not found or does not belong to the specified governorate", HttpStatus.BAD_REQUEST),
    TOO_MANY_PREFERRED_AREAS("PROFILE_006", "Maximum 5 preferred areas allowed", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("PROFILE_007", "User not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
