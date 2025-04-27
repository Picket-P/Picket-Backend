package com.example.picket.domain.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TemporarySaveRequest {

    @Schema(description = "임시 저장할 orderId")
    @NotNull(message = "orderId는 필수 입력값입니다.")
    private String orderId;

    @Schema(description = "임시 저장할 amount")
    @NotNull(message = "amount는 필수 입력값입니다.")
    private BigDecimal amount;

    public TemporarySaveRequest(String orderId, BigDecimal amount) {
        this.orderId = orderId;
        this.amount = amount;
    }

    public static TemporarySaveRequest from(String orderId, BigDecimal amount) {
        return new TemporarySaveRequest(orderId, amount);
    }
}
