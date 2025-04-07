package com.example.picket.domain.seat.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class SeatUpdateRequest {

    private String grade;       // 등급 (예: VIP)
    private int quantity;       // 좌석 수
    private BigDecimal price;   // 좌석 가격
}
