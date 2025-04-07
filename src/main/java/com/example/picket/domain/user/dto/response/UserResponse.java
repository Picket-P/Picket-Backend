package com.example.picket.domain.user.dto.response;

import com.example.picket.common.enums.Gender;
import com.example.picket.common.enums.UserRole;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UserResponse {
    private final String email;
    private final UserRole userRole;
    private final String profileUrl;
    private final String nickname;
    private final LocalDate birth;
    private final Gender gender;

    private UserResponse(String email, UserRole userRole, String profileUrl, String nickname, LocalDate birth, Gender gender) {
        this.email = email;
        this.userRole = userRole;
        this.profileUrl = profileUrl;
        this.nickname = nickname;
        this.birth = birth;
        this.gender = gender;
    }

    public static UserResponse toDto(String email, UserRole userRole, String profileUrl, String nickname, LocalDate birth, Gender gender) {
        return new UserResponse(email, userRole, profileUrl, nickname, birth, gender);
    }
}
