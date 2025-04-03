package com.example.picket.common.enums;

import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import java.util.Arrays;

public enum UserRole {
    USER, ADMIN, DIRECTOR;

    public static UserRole of(String type) {
        return Arrays.stream(UserRole.values())
                .filter(t -> t.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER_TYPE));
    }
}