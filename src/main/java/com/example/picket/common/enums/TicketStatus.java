package com.example.picket.common.enums;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.example.picket.common.exception.CustomException;
import java.util.Arrays;

public enum TicketStatus {

    TICKET_CREATED("예매완료"),
    TICKET_CANCELED("취소"),
    TICKET_EXPIRED("만료");

    private final String description;

    TicketStatus(String description) {
        this.description = description;
    }

    public static TicketStatus of(String type) {
        return Arrays.stream(TicketStatus.values())
                .filter(t -> t.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new CustomException(BAD_REQUEST, "유효하지 않은 티켓 유형입니다."));
    }
}