package com.example.picket.common.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

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
    INVALID_GRADE_TYPE(BAD_REQUEST, "유효하지 않은 등급 유형입니다."),
    INVALID_AUTH_ANNOTATION_USAGE(BAD_REQUEST, "@Auth와 AuthUser 타입은 함께 사용되어야 합니다."),
    INVALID_PASSWORD(UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    // M


    // N
    NOT_FOUND_USER(NOT_FOUND, "해당 유저를 찾을 수 없습니다."),

    // R


    // S

    // U
    UNAUTHORIZED_LOGIN(UNAUTHORIZED, "로그인 이후 이용 가능합니다."), ;

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
