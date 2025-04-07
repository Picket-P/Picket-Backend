package com.example.picket.domain.seat.dto;

import com.example.picket.common.enums.Grade;
import com.example.picket.common.enums.SeatStatus;
import com.example.picket.domain.seat.entity.Seat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeatDetailResponse {

    private Long id;
    private int seatNumber;
    private String formattedNumber;
    private SeatStatus status;

    public static SeatDetailResponse from(Seat seat) {
        return new SeatDetailResponse(
                seat.getId(),
                seat.getSeatNumber(),
                formatSeatNumber(seat.getGrade(), seat.getSeatNumber()),
                seat.getSeatStatus()
        );
    }

    private static String formatSeatNumber(Grade grade, int seatNumber) {
        return grade.name() + String.format("%02d", seatNumber);
    }
}

