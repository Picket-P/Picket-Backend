package com.example.picket.common.enums;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.example.picket.common.exception.CustomException;
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
                .orElseThrow(() -> new CustomException(BAD_REQUEST, "유효하지 않은 카테고리 유형입니다."));
    }
}