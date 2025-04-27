package com.example.picket.domain.email.controller;

import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.email.dto.request.EmailResendRequest;
import com.example.picket.domain.email.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/email")
@Tag(name = "이메일 API", description = "이메일 전송 및 재시도 관련 API입니다.")
public class EmailController {

    private final EmailService emailService;

    @Operation(summary = "인증 이메일 재전송", description = "인증 코드를 이메일로 다시 전송합니다.")
    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationEmail(@Valid @RequestBody EmailResendRequest request) {
        try {
            emailService.sendVerificationEmail(request.getEmail());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (CustomException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(Map.of("message", e.getMessage()));
        }
    }
}