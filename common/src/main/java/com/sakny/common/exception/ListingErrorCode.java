package com.sakny.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ListingErrorCode implements ErrorCode {

    LISTING_NOT_FOUND("LISTING_001", "Listing not found", HttpStatus.NOT_FOUND),
    LISTING_NOT_OWNED("LISTING_002", "You do not have permission to modify this listing", HttpStatus.FORBIDDEN),
    INVALID_BUDGET_RANGE("LISTING_003", "Rent amount must be positive", HttpStatus.BAD_REQUEST),
    INVALID_GOVERNORATE("LISTING_004", "Governorate not found", HttpStatus.BAD_REQUEST),
    INVALID_CITY("LISTING_005", "City not found or does not belong to the specified governorate", HttpStatus.BAD_REQUEST),
    INVALID_AVAILABILITY_DATE("LISTING_006", "Available from date must be in the future or today", HttpStatus.BAD_REQUEST),
    TOO_MANY_IMAGES("LISTING_007", "Maximum 10 images allowed per listing", HttpStatus.BAD_REQUEST),
    INVALID_ROOMMATE_COUNT("LISTING_008", "Current roommates cannot exceed total roommates", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("LISTING_009", "User not found", HttpStatus.NOT_FOUND),
    LISTING_ALREADY_CLOSED("LISTING_010", "Listing is already closed", HttpStatus.BAD_REQUEST),
    PROFILE_REQUIRED("LISTING_011", "You must complete your profile before creating a listing", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}

