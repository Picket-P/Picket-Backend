package com.example.picket.domain.seat.dto;

import com.example.picket.common.enums.Grade;
import com.example.picket.domain.seat.entity.Seat;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class SeatGroupByGradeResponse {
    private Grade grade;
    private BigDecimal price;
    private List<SeatDetailResponse> seats;

    public SeatGroupByGradeResponse(Grade grade, BigDecimal price, List<SeatDetailResponse> seats) {
        this.grade = grade;
        this.price = price;
        this.seats = seats;
    }

    public static SeatGroupByGradeResponse toDto(Grade grade, BigDecimal price, List<Seat> seats) {
        return new SeatGroupByGradeResponse(
                grade,
                price,
                seats.stream()
                        .map(SeatDetailResponse::toDto)
                        .toList()
        );
    }
}

