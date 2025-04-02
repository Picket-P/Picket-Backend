package com.example.picket.common.exception;

import static org.springframework.http.HttpStatus.CONFLICT;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // A

    // D 예시
    DUPLICATE_EMAIL(CONFLICT, "이미 가입되어있는 이메일 입니다.");

    // E


    // F


    // I


    // M


    // N


    // R


    // S


    // U


    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
