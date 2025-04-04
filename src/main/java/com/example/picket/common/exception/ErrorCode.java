package com.example.picket.common.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {


    // AUTH
    AUTH_UNAUTHORIZED_LOGIN(UNAUTHORIZED, "로그인 이후 이용 가능합니다."),

    // AUTHUSER
    AUTH_ANNOTATION_INVALID_USAGE(BAD_REQUEST, "@Auth와 AuthUser 타입은 함께 사용되어야 합니다."),

    // COMMENT


    // ENUM
    USER_TYPE_INVALID(BAD_REQUEST, "유효하지 않은 사용자 유형입니다."),
    GENDER_TYPE_INVALID(BAD_REQUEST, "유효하지 않은 성별 유형입니다."),
    CATEGORY_TYPE_INVALID(BAD_REQUEST, "유효하지 않은 카테고리 유형입니다."),
    GRADE_TYPE_INVALID(BAD_REQUEST, "유효하지 않은 등급 유형입니다."),

    // LIKE
    LIKE_NOT_FOUND(NOT_FOUND, "해당 좋아요를 찾을 수 없습니다."),
    LIKE_REQUEST_USER_MISMATCH(BAD_REQUEST, "해당 좋아요를 누른 사용자와 요청한 사용자가 다릅니다."),
    LIKE_REQUEST_SHOW_MISMATCH(BAD_REQUEST, "해당 좋아요가 눌린 공연과 요청된 공연이 다릅니다."),
    // SEAT


    // SHOW
    SHOW_NOT_FOUND(NOT_FOUND, "해당 공연을 찾을 수 없습니다."),

    // SHOW_DATE


    // TICKET
    TICKET_TYPE_INVALID(BAD_REQUEST, "유효하지 않은 티켓 유형입니다."),


    // USER
    USER_DUPLICATE_EMAIL(BAD_REQUEST, "이미 가입되어있는 이메일 입니다."),
    USER_PASSWORD_INVALID(UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    USER_NOT_FOUND(NOT_FOUND, "해당 유저를 찾을 수 없습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
