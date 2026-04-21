package com.sakny.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ErrorCode {
    RESERVATION_NOT_FOUND("RES_001", "Reservation not found", HttpStatus.NOT_FOUND),
    PROPERTY_NOT_AVAILABLE("RES_002", "Property is not available for the selected dates", HttpStatus.CONFLICT),
    INVALID_RESERVATION_DATES("RES_003", "Start date must be before end date and in the future", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_RESERVATION_ACCESS("RES_004", "You are not authorized to access this reservation", HttpStatus.FORBIDDEN),
    INVALID_RESERVATION_STATUS_CHANGE("RES_005", "Invalid reservation status change", HttpStatus.BAD_REQUEST),
    OWNER_CANNOT_RESERVE_OWN_PROPERTY("RES_006", "Owners cannot reserve their own properties", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
