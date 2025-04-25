package com.example.picket.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailVerificationRequest {

    @Email(message = "유효한 이메일 형식이어야 합니다.")
    private String email;
}
