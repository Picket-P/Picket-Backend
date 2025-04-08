package com.example.picket.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdatePasswordRequest {

    @Schema(description = "비밀번호", example = "기존 비밀번호입니다.")
    @NotBlank
    private String password;

    @Schema(description = "새 비밀번호", example = "새로 변경할 비밀번호입니다.")
    @NotBlank
    private String newPassword;
}
