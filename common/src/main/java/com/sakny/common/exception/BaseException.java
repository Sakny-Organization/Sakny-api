package com.sakny.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract sealed class BaseException extends RuntimeException 
        permits BusinessException {
    
    private final String errorCode;
    private final HttpStatus httpStatus;

    protected BaseException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    protected BaseException(String message, Throwable cause, 
                          String errorCode, HttpStatus httpStatus) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}