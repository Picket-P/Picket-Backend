package com.example.picket.domain.seat.dto.response;

import com.example.picket.common.enums.Grade;
import lombok.Getter;

@Getter
public class SeatSummaryResponse {

    private final Grade grade;
    private final int total;
    private final int reserved;
    private final int available;

    private SeatSummaryResponse(Grade grade, int total, int reserved, int available) {
        this.grade = grade;
        this.total = total;
        this.reserved = reserved;
        this.available = available;
    }

    public static SeatSummaryResponse toDto(Grade grade, int total, int reserved, int available) {
        return new SeatSummaryResponse(grade, total, reserved, available);
    }

}

