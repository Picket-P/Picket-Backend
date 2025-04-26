package com.example.picket.domain.payment.dto.request;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TemporarySaveRequest {

    private String orderId;

    private BigDecimal amount;

    public TemporarySaveRequest(String orderId, BigDecimal amount) {
        this.orderId = orderId;
        this.amount = amount;
    }

    public static TemporarySaveRequest from(String orderId, BigDecimal amount) {
        return new TemporarySaveRequest(orderId, amount);
    }
}
