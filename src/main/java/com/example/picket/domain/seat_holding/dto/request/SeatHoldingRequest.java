package com.example.picket.domain.seat_holding.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SeatHoldingRequest {

    @Schema(description = "선점할 좌석 ID", example = "[1]")
    @NotNull(message = "좌석 ID는 필수 입력값입니다.")
    private List<Long> seatIds = new ArrayList<>();
}
