package com.example.picket.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawUserRequest {

    @Schema(description = "비밀번호", example = "!Password1234")
    @NotBlank
    private String password;
}
