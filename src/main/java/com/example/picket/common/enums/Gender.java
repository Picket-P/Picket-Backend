package com.example.picket.common.enums;

import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import java.util.Arrays;

public enum Gender {
    MALE, FEMALE;

    public static Gender of(String type) {
        return Arrays.stream(Gender.values())
                .filter(t -> t.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_GENDER_TYPE));
    }
}