package com.example.picket.common.enums;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.example.picket.common.exception.CustomException;
import java.util.Arrays;

public enum UserRole {
    USER, ADMIN, DIRECTOR;

    public static UserRole of(String type) {
        return Arrays.stream(UserRole.values())
                .filter(t -> t.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new CustomException(BAD_REQUEST, "유효하지 않은 사용자 유형입니다."));
    }
}