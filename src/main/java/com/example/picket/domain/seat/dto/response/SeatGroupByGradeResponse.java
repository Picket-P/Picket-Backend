package com.example.picket.domain.seat.dto.response;

import com.example.picket.common.enums.Grade;
import com.example.picket.domain.seat.entity.Seat;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;

@Getter
public class SeatGroupByGradeResponse {
    private Grade grade;
    private BigDecimal price;
    private List<SeatDetailResponse> seats;

    private SeatGroupByGradeResponse(Grade grade, BigDecimal price, List<SeatDetailResponse> seats) {
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

