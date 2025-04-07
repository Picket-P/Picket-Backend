package com.example.picket.domain.seat.dto.response;

import com.example.picket.common.enums.Grade;
import com.example.picket.common.enums.SeatStatus;
import com.example.picket.domain.seat.entity.Seat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeatDetailResponse {

    private Long id;
    private int seatNumber;
    private String formattedNumber;
    private SeatStatus status;

    private SeatDetailResponse(Seat seat) {
        this.id = seat.getId();
        this.seatNumber = seat.getSeatNumber();
        this.formattedNumber = formatSeatNumber(seat.getGrade(), seat.getSeatNumber());
        this.status = seat.getSeatStatus();
    }

    public static SeatDetailResponse toDto(Seat seat) {
        return new SeatDetailResponse(seat);
    }

    private static String formatSeatNumber(Grade grade, int seatNumber) {
        return grade.name() + String.format("%02d", seatNumber);
    }
}

