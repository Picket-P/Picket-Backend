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
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/email")
@Tag(name = "이메일 API", description = "이메일 전송 및 재시도 관련 API입니다.")
public class EmailController {

    private final EmailService emailService;

    @Operation(summary = "인증 이메일 재전송", description = "인증 코드를 이메일로 다시 전송합니다.")
    @PostMapping("/resend")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(@Valid @RequestBody EmailResendRequest request) {
        return Optional.ofNullable(request.getEmail())
                .map(email -> {
                    emailService.sendVerificationEmail(email);
                    return ResponseEntity.ok(Map.of("message", "인증 이메일이 성공적으로 재전송되었습니다."));
                })
                .orElseGet(() -> ResponseEntity.badRequest()
                        .body(Map.of("message", "이메일 정보가 필요합니다.")));
    }
}