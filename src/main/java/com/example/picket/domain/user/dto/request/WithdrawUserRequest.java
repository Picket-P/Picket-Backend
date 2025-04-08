package com.example.picket.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WithdrawUserRequest {

    @Schema(description = "비밀번호", example = "기존 비밀번호입니다.")
    @NotBlank
    private String password;
}
