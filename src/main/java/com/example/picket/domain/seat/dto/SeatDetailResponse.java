package com.example.picket.domain.seat.dto;

import com.example.picket.common.enums.SeatStatus;
import com.example.picket.domain.seat.entity.Seat;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SeatDetailResponse {
    private Long id;
    private int seatNumber;
    private String formattedNumber;
    private SeatStatus status;

    public static SeatDetailResponse from(Seat seat) {
        return SeatDetailResponse.builder()
                .id(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .formattedNumber(seat.getGrade().name() + String.format("%02d", seat.getSeatNumber()))
                .status(seat.getSeatStatus())
                .build();
    }
}

