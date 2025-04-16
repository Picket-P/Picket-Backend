package com.example.picket.common.enums;

import com.example.picket.common.exception.CustomException;

import java.util.Arrays;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public enum OrderStatus {
    ORDER_PENDING("결제 대기"),
    ORDER_COMPLETE("주문 완료");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public static OrderStatus of(String type) {
        return Arrays.stream(OrderStatus.values())
                .filter(t -> t.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new CustomException(BAD_REQUEST, "유효하지 않은 주문 유형입니다."));
    }
}
