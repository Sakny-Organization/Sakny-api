package com.sakny.common.exception;

import lombok.Getter;

@Getter
public final class BusinessException extends BaseException {

    private final ErrorCode domainError;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage(), errorCode.getCode(), errorCode.getHttpStatus());
        this.domainError = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage, errorCode.getCode(), errorCode.getHttpStatus());
        this.domainError = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause, errorCode.getCode(), errorCode.getHttpStatus());
        this.domainError = errorCode;
    }
}