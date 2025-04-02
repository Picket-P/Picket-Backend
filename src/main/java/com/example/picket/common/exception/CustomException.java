package com.example.picket.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private final HttpStatus status;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.status = errorCode.getHttpStatus();
    }

    public CustomException(ErrorCode errorCode, String message) {
        super(errorCode.getMessage() + message);
        this.status = errorCode.getHttpStatus();
    }

}