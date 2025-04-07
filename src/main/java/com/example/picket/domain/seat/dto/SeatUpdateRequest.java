package com.example.picket.domain.seat.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class SeatUpdateRequest {

    @NotNull(message = "등급(grade)은 필수 입력값입니다.")
    private String grade;       // 등급 (예: VIP)

    @Min(value = 1, message = "좌석 수는 1개 이상이어야 합니다.")
    private int quantity;       // 좌석 수

    @NotNull(message = "가격은 필수 입력값입니다.")
    @Positive(message = "가격은 0보다 커야 합니다.")
    private BigDecimal price;   // 좌석 가격
}
