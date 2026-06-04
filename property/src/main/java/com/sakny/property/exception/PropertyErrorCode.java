package com.sakny.property.exception;

import com.sakny.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PropertyErrorCode implements ErrorCode {
    PROPERTY_NOT_FOUND("PROP_001", "Property not found", HttpStatus.NOT_FOUND),
    NOT_PROPERTY_OWNER("PROP_002", "You are not the owner of this property", HttpStatus.FORBIDDEN),
    GOVERNORATE_NOT_FOUND("PROP_003", "Governorate not found", HttpStatus.BAD_REQUEST),
    CITY_NOT_FOUND("PROP_004", "City not found", HttpStatus.BAD_REQUEST),
    CITY_GOVERNORATE_MISMATCH("PROP_005", "City does not belong to the given governorate", HttpStatus.BAD_REQUEST),
    IMAGE_NOT_FOUND("PROP_006", "Image not found", HttpStatus.NOT_FOUND),
    IMAGE_PROPERTY_MISMATCH("PROP_007", "Image does not belong to this property", HttpStatus.FORBIDDEN),
    USER_NOT_FOUND("PROP_008", "User not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
