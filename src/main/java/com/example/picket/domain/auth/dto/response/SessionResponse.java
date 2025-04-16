package com.example.picket.domain.auth.dto.response;

import com.example.picket.common.enums.UserRole;
import lombok.Getter;

@Getter
public class SessionResponse {

    private final String nickname;
    private final String email;
    private final UserRole role;

    private SessionResponse(String nickname, String email, UserRole role) {
        this.nickname = nickname;
        this.email = email;
        this.role = role;
    }

    public static SessionResponse toDto(String nickname, String email, UserRole role) {
        return new SessionResponse(nickname, email, role);
    }
}
