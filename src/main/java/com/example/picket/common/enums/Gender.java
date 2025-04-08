package com.example.picket.common.enums;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.example.picket.common.exception.CustomException;
import java.util.Arrays;

public enum Gender {
    MALE, FEMALE;

    public static Gender of(String type) {
        return Arrays.stream(Gender.values())
                .filter(t -> t.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new CustomException(BAD_REQUEST, "유효하지 않은 성별 유형입니다."));
    }
}