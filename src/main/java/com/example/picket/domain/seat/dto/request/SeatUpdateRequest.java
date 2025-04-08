package com.example.picket.domain.seat.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatUpdateRequest {

    @NotNull(message = "등급(grade)은 필수 입력값입니다.")
    private String grade;       // 등급 (예: VIP)

    @Min(value = 1, message = "좌석 수는 1개 이상이어야 합니다.")
    private int quantity;       // 좌석 수

    @NotNull(message = "가격은 필수 입력값입니다.")
    @Positive(message = "가격은 0보다 커야 합니다.")
    private BigDecimal price;   // 좌석 가격

    private SeatUpdateRequest(String grade, int quantity, BigDecimal price) {
        this.grade = grade;
        this.quantity = quantity;
        this.price = price;
    }

    public static SeatUpdateRequest toDto(String grade, int quantity, BigDecimal price) {
        return new SeatUpdateRequest(grade, quantity, price);
    }
}
