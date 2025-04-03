package com.example.picket.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SigninResponse {

    private final String nickname;

    @Builder
    private SigninResponse(String nickname) {
        this.nickname = nickname;
    }
}
