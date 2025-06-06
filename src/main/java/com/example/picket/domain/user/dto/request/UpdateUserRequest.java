package com.example.picket.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {

    @Schema(description = "비밀번호", example = "!Password1234")
    @NotBlank
    private String password;

    @Schema(description = "닉네임", example = "변경할 닉네임입니다.")
    @NotBlank
    private String nickname;

    @Schema(description = "프로필", example = "새롭게 변경할 프로필 URL입니다.")
    @NotBlank
    private String profileUrl;
}
