package com.example.picket.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SigninResponse {

    private final String sessionId;
    private final String nickname;

    @Builder
    private SigninResponse(String sessionId, String nickname) {
        this.sessionId = sessionId;
        this.nickname = nickname;
    }
}
