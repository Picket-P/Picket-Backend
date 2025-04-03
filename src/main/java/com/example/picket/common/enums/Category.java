package com.example.picket.common.enums;

import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import java.util.Arrays;


public enum Category {
    MUSICAL("뮤지컬"), PLAY("연극"), CONCERT("콘서트"), CLASSIC("클래식"), SPORT("스포츠"), OTHER("기타");

    private final String description;

    Category(String description) {
        this.description = description;
    }

    public static Category of(String type) {
        return Arrays.stream(Category.values())
                .filter(t -> t.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CATEGORY_TYPE));
    }
}