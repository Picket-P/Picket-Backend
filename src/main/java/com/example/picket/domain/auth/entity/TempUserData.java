package com.example.picket.domain.auth.entity;

import com.example.picket.common.enums.Gender;
import com.example.picket.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TempUserData {
    private String email;
    private String password;  // 인코딩된 비밀번호
    private String nickname;
    private LocalDate birth;
    private Gender gender;
    private UserRole userRole;
}