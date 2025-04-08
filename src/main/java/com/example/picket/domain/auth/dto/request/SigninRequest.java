package com.example.picket.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SigninRequest {

    @Schema(description = "이메일", example = "test@example.com")
    @NotBlank(message = "이메일 입력은 필수입니다.")
    private String email;

    @Schema(description = "비밀번호", example = "!Password1234")
    @NotBlank(message = "비밀번호 입력은 필수입니다.")
    private String password;

}
