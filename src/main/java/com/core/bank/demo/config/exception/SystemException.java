package com.core.bank.demo.config.exception;

import lombok.Getter;

@Getter
public class SystemException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object details;

    public SystemException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    public SystemException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }

    public SystemException(ErrorCode errorCode, String message, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public SystemException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = null;
    }

    public SystemException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.details = null;
    }
}
