package com.example.picket.domain.seat.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class SeatUpdateRequest {

    private String grade;
    private int quantity;
    private BigDecimal price;
}
