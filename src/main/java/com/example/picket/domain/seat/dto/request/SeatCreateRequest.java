package com.example.picket.domain.seat.dto.request;

import com.example.picket.common.enums.Grade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class SeatCreateRequest {

    @Schema(description = "좌석 등급", example = "VIP")
    @NotNull(message = "Grade는 필수 입력값입니다.")
    private Grade grade; // VIP, A, R ...

    @Schema(description = "좌석 수", example = "100")
    @Min(value = 1, message = "좌석 수는 1개 이상이어야 합니다.")
    private int seatCount;

    @Schema(description = "좌석 가격", example = "100000")
    @NotNull(message = "가격은 필수 입력값입니다.")
    @Positive(message = "가격은 0보다 커야 합니다.")
    private BigDecimal price;
}
