package com.example.picket.domain.seat.dto.request;

import com.example.picket.common.enums.Grade;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class SeatCreateRequest {

    @NotNull(message = "Grade는 필수 입력값입니다.")
    private Grade grade; // VIP, A, R ...

    @Min(value = 1, message = "좌석 수는 1개 이상이어야 합니다.")
    private int seatCount;

    @NotNull(message = "가격은 필수 입력값입니다.")
    @Positive(message = "가격은 0보다 커야 합니다.")
    private BigDecimal price;
}
