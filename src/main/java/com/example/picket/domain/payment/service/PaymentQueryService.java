package com.example.picket.domain.payment.service;

import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.order.entity.Order;
import com.example.picket.domain.payment.entity.Payment;
import com.example.picket.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PaymentQueryService {

    private final PaymentRepository paymentRepository;

    public Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId).orElseThrow(
                () -> new CustomException(NOT_FOUND, "존재하지 않는 Payment입니다."));
    }
}
