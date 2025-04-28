package com.example.picket.domain.payment.service;

import com.example.picket.domain.payment.entity.Payment;
import com.example.picket.domain.payment.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentQueryServiceTest {

    @InjectMocks
    private PaymentQueryService paymentQueryService;

    @Mock
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("성공적으로 Payment를 조회할 수 있다.")
    void canGetPayment() {
        // given
        Long paymentId = 1L;
        Payment mockPayment = mock(Payment.class);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(mockPayment));

        // when
        Payment result= paymentQueryService.getPayment(paymentId);

        // then
        assertNotNull(result);
        verify(paymentRepository).findById(paymentId);
    }

}