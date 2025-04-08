package com.example.picket.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdatePasswordRequest {

    @Schema(description = "비밀번호", example = "Aodlstory321!")
    @NotBlank
    private String password;

    @Schema(description = "새 비밀번호", example = "Aodlstory123!")
    @NotBlank
    private String newPassword;
}
