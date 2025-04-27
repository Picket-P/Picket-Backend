package com.example.picket.domain.payment.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.domain.payment.dto.request.TemporarySaveRequest;
import com.example.picket.domain.payment.service.PaymentCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v2/payments")
@RequiredArgsConstructor
@Tag(name = "결제 정보 임시저장 API", description = "결제 정보 임시 저장 API입니다.")
public class PaymentController {

    private final PaymentCommandService paymentCommandService;

    @PostMapping("/temporary-save")
    @Operation(summary = "결제 정보 임시 저장", description = "결제 승인 전 결제 정보를 임시 저장합니다.")
    public ResponseEntity<Void> temporarySavePaymentInfo(
            @Auth AuthUser authUser,
            @RequestBody TemporarySaveRequest dto) {
        paymentCommandService.temporarySavePaymentInfo(dto.getOrderId(), dto.getAmount(), authUser.getId());
        return ResponseEntity.ok().build();
    }
}
