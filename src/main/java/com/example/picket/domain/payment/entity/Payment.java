package com.example.picket.domain.payment.entity;

import com.example.picket.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tossPaymentKey;

    @Column(nullable = false)
    private String tossOrderId;

    @Column(nullable = false)
    private String tossOrderName;

    @Column(nullable = false)
    private Number tossAmount;

    @Column(nullable = false)
    private String tossStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    public void setOrder(Order order) {
        this.order = order;
    }

    public Payment(String tossPaymentKey, String tossOrderId, String tossOrderName, Number tossAmount, String tossStatus, Order order) {
        this.tossPaymentKey = tossPaymentKey;
        this.tossOrderId = tossOrderId;
        this.tossOrderName = tossOrderName;
        this.tossAmount = tossAmount;
        this.tossStatus = tossStatus;
        this.order = order;
    }

    public static Payment create(String tossPaymentKey, String tossOrderId, String tossOrderName, Number tossAmount, String tossStatus, Order order) {
        return new Payment(tossPaymentKey, tossOrderId, tossOrderName, tossAmount, tossStatus, order);
    }
}
