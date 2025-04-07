package com.example.picket.domain.seat.dto;

import com.example.picket.common.enums.Grade;
import com.example.picket.domain.seat.entity.Seat;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class SeatGroupByGradeResponse {
    private Grade grade;
    private BigDecimal price;
    private List<SeatDetailResponse> seats;

    public static SeatGroupByGradeResponse from(Grade grade, BigDecimal price, List<Seat> seats) {
        return SeatGroupByGradeResponse.builder()
                .grade(grade)
                .price(price)
                .seats(seats.stream()
                        .map(SeatDetailResponse::from)
                        .toList())
                .build();
    }
}

