package com.example.picket.domain.user.dto.request;

import com.example.picket.common.consts.Const;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePasswordRequest {

    @Schema(description = "비밀번호", example = "!Password1234")
    @NotBlank
    private String password;

    @Schema(description = "새 비밀번호", example = "!Password12345")
    @Pattern(
            regexp = Const.PASSWORD_PATTERN,
            message = "비밀번호 형식이 올바르지 않습니다."
    )
    @NotBlank
    private String newPassword;
}
