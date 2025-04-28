package com.example.picket.domain.payment.service;

import com.example.picket.domain.order.entity.Order;
import com.example.picket.domain.payment.entity.Payment;
import com.example.picket.domain.payment.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCommandServiceTest {

    @InjectMocks
    private PaymentCommandService paymentCommandService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Test
    @DisplayName("결제정보를 임시로 저장 할 수 있다.")
    void canTemporarilySavePaymentInfo() {
        // given
        String orderId = "dafadsfasd";
        BigDecimal amount = new BigDecimal("100");
        Long userId = 1L;
        String key = "PAYMENT-INFO:USER:" + userId;
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);

        // when
        paymentCommandService.temporarySavePaymentInfo(orderId, amount, userId);

        // then
        verify(hashOperations).put(key, "orderId", orderId);
        verify(hashOperations).put(key, "amount", amount.toString());
        verify(redisTemplate).expire(key, 600, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Payment를 성공적으로 저장할 수 있다.")
    void canCreatePayment() {
        // given
        String paymentKey = "qdfqdfadsf";
        String orderId = "dafadsfasd";
        String orderName = "레베카 VIP S12";
        BigDecimal amount = new BigDecimal("100");
        String status = "DONE";
        Order orderMock = mock(Order.class);

        // when
        paymentCommandService.createPayment(paymentKey, orderId, orderName, status, amount, orderMock);

        // then
        verify(paymentRepository).save(any(Payment.class));
    }
}