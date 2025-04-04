package com.example.picket.domain.seat.dto;

import com.example.picket.common.enums.Grade;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class SeatCreateRequest {

    private Grade grade; // VIP, A, R ...
    private int seatCount;
    private BigDecimal price;
}
