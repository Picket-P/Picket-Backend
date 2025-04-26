package com.example.picket.domain.payment.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.domain.payment.dto.request.TemporarySaveRequest;
import com.example.picket.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v2/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/temporary-save")
    public ResponseEntity<Void> temporarySavePaymentInfo(
            @Auth AuthUser authUser,
            @RequestBody TemporarySaveRequest dto) {
        paymentService.temporarySavePaymentInfo(dto.getOrderId(), dto.getAmount(), authUser.getId());
        return ResponseEntity.ok().build();
    }
}
