package com.example.picket.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class SigninResponse {

    private final String nickname;

    private SigninResponse(String nickname) {
        this.nickname = nickname;
    }

    public static SigninResponse toDto(String nickname) {
        return new SigninResponse(nickname);
    }
}
