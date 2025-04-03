package com.example.picket.common.enums;

import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import java.util.Arrays;

public enum Grade {
    VIP, R, S, A, B, ALL;

    public static Grade of(String type) {
        return Arrays.stream(Grade.values())
                .filter(t -> t.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_GRADE_TYPE));
    }
}