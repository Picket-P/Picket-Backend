package com.example.picket.domain.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String KEY_PREFIX = "PAYMENT-INFO:USER:";
    private final StringRedisTemplate redisTemplate;

    public void temporarySavePaymentInfo(String orderId, BigDecimal amount, Long userId) {
        String userIdToSave = KEY_PREFIX + userId.toString();
        String orderIdToSave = orderId;
        BigDecimal amountToSave = amount;

        redisTemplate.opsForHash().put(userIdToSave, "orderId", orderIdToSave);
        redisTemplate.opsForHash().put(userIdToSave, "amount", amountToSave.toString());

        redisTemplate.expire(userIdToSave, 600, TimeUnit.SECONDS);
    }
}
