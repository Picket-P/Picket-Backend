package com.example.picket.domain.booking.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
public class BookingRequest {

    @Schema(description = "예매할 좌석 ID", example = "[1]")
    @NotNull(message = "좌석 ID는 필수 입력값입니다.")
    private List<Long> seatIds = new ArrayList<>();

    @Schema(description = "예매할 좌석 ID")
    @NotNull(message = "paymentKey는 필수 입력값입니다.")
    private String paymentKey;

    @Schema(description = "예매할 좌석 ID")
    @NotNull(message = "orderId는 필수 입력값입니다.")
    private String orderId;

    @Schema(description = "예매할 좌석 ID", example = "300")
    @NotNull(message = "amount는 필수 입력값입니다.")
    private Number amount;
}
