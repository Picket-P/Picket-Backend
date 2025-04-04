package com.example.picket.domain.seat.dto;

import com.example.picket.common.enums.Grade;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class SeatResponse {
    private String seatNumber;
    private Grade grade;
    private BigDecimal price;
}
