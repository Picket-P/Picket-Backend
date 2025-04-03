package com.example.picket.domain.auth.dto.request;

import com.example.picket.common.consts.Const;
import com.example.picket.common.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignupRequest {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일 입력은 필수입니다.")
    private String email;

    @Pattern(
            regexp = Const.PASSWORD_PATTERN,
            message = "비밀번호 형식이 올바르지 않습니다."
    )
    private String password;

    @NotBlank(message = "닉네임 입력은 필수입니다.")
    private String nickname;

    @NotNull(message = "생년월일은 필수 입니다.")
    @Past(message = "생년월일은 과거여야 합니다.")
    private LocalDate birth;

    @NotNull(message = "성별은 필수 입니다.")
    private Gender gender;

}
