package com.example.picket.common.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // A

    // D 예시
    DUPLICATE_EMAIL(CONFLICT, "이미 가입되어있는 이메일 입니다."),


    // E


    // F


    // I
    INVALID_USER_TYPE(BAD_REQUEST, "유효하지 않은 사용자 유형입니다."),
    INVALID_GENDER_TYPE(BAD_REQUEST, "유효하지 않은 성별 유형입니다."),
    INVALID_CATEGORY_TYPE(BAD_REQUEST, "유효하지 않은 카테고리 유형입니다."),
    INVALID_GRADE_TYPE(BAD_REQUEST, "유효하지 않은 등급 유형입니다.");

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
