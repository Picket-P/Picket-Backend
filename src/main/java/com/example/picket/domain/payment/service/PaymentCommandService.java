package com.example.picket.domain.payment.service;

import com.example.picket.domain.order.entity.Order;
import com.example.picket.domain.payment.entity.Payment;
import com.example.picket.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PaymentCommandService {

    private static final String KEY_PREFIX = "PAYMENT-INFO:USER:";
    private final StringRedisTemplate redisTemplate;
    private final PaymentRepository paymentRepository;

    public void temporarySavePaymentInfo(String orderId, BigDecimal amount, Long userId) {
        String userIdToSave = KEY_PREFIX + userId.toString();
        String orderIdToSave = orderId;
        BigDecimal amountToSave = amount;

        redisTemplate.opsForHash().put(userIdToSave, "orderId", orderIdToSave);
        redisTemplate.opsForHash().put(userIdToSave, "amount", amountToSave.toString());

        redisTemplate.expire(userIdToSave, 600, TimeUnit.SECONDS);
    }

    public Payment createPayment(String paymentKey, String orderId, String orderName, String status, BigDecimal totalAmount, Order order) {
        Payment payment = Payment.create(paymentKey, orderId, orderName, totalAmount, status, order);
        return paymentRepository.save(payment);
    }
}
