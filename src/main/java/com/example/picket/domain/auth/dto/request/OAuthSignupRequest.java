package com.example.picket.domain.auth.dto.request;

import com.example.picket.common.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthSignupRequest {

    @Schema(description = "닉네임", example = "nickname1")
    @NotBlank(message = "닉네임 입력은 필수입니다.")
    private String nickname;

    @Schema(description = "생년월일", example = "2000-12-12")
    @NotNull(message = "생년월일은 필수 입니다.")
    @Past(message = "생년월일은 과거여야 합니다.")
    private LocalDate birth;

    @Schema(description = "성별", example = "MALE")
    @NotNull(message = "성별은 필수 입니다.")
    private Gender gender;
}
