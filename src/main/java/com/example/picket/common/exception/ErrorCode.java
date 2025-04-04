package com.example.picket.common.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
public enum ErrorCode {

    // A
    AFTER_SHOW_RESERVATION_TIME(BAD_REQUEST, "예매 종료 시간 이후 입니다."),
    ALREADY_RESERVED_SEAT(CONFLICT, "이미 선점된 좌석입니다."),

    // B
    BEFORE_SHOW_RESERVATION_TIME(BAD_REQUEST, "예매 시작 시간 전입니다."),

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
    LIKE_ALREADY_EXIST(BAD_REQUEST, "이미 해당 좋아요를 눌렀습니다."),

    // SEAT
    INVALID_AUTH_ANNOTATION_USAGE(BAD_REQUEST, "@Auth와 AuthUser 타입은 함께 사용되어야 합니다."),
    INVALID_PASSWORD(UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    // M


    // SHOW
    // N
    NO_AVAILABLE_SEAT(CONFLICT, "남아있는 좌석이 없습니다."),

    SHOW_NOT_FOUND(NOT_FOUND, "해당 공연을 찾을 수 없습니다."),

    // SHOW_DATE
    // O
    ONLY_USER_CAN_RESERVATION(FORBIDDEN, "오직 USER만 예매 가능합니다."),

    // R


    // TICKET
    TICKET_TYPE_INVALID(BAD_REQUEST, "유효하지 않은 티켓 유형입니다."),

    // S
    SHOW_NOT_FOUND(NOT_FOUND, "존재하지 않는 Show입니다."),
    SHOW_DATE_NOT_FOUND(NOT_FOUND, "존재하지 않는 ShowDate입니다."),
    SEAT_NOT_FOUND(NOT_FOUND, "존재하지 않는 Seat입니다."),
    SHOW_RESERVATION_TIME_INVALID_AFTER_SHOW(BAD_REQUEST, "예매 종료 시간 이후 입니다."),
    SEAT_ALREADY_RESERVED(CONFLICT, "이미 예매된 좌석입니다."),
    SHOW_RESERVATION_TIME_INVALID_BEFORE_SHOW(BAD_REQUEST, "예매 시작 시간 전입니다."),
    SEAT_NO_AVAILABLE(CONFLICT, "남아있는 좌석이 없습니다."),


    // T
    TICKET_NOT_FOUND(NOT_FOUND, "존재하지 않는 Ticket입니다."),

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
