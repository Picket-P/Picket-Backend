package com.example.picket.domain.payment.entity;

import com.example.picket.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private String tossAmount;

    @Column(nullable = false)
    private String tossStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    public void setOrder(Order order) {
        this.order = order;
    }

    public Payment(String tossPaymentKey, String tossOrderId, String tossOrderName, String tossAmount, String tossStatus) {
        this.tossPaymentKey = tossPaymentKey;
        this.tossOrderId = tossOrderId;
        this.tossOrderName = tossOrderName;
        this.tossAmount = tossAmount;
        this.tossStatus = tossStatus;
    }

    public static Payment create(String tossPaymentKey, String tossOrderId, String tossOrderName, String tossAmount, String tossStatus) {
        return new Payment(tossPaymentKey, tossOrderId, tossOrderName, tossAmount, tossStatus);
    }
}
