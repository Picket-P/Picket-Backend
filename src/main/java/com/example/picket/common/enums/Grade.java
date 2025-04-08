package com.example.picket.common.enums;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.example.picket.common.exception.CustomException;
import java.util.Arrays;

public enum Grade {
    VIP, R, S, A, B, ALL;

    public static Grade of(String type) {
        return Arrays.stream(Grade.values())
                .filter(t -> t.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new CustomException(BAD_REQUEST, "유효하지 않은 등급 유형입니다."));
    }
}