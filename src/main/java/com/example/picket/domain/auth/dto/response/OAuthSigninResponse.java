package com.example.picket.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class OAuthSigninResponse {

    private final String nickname;
    private final Boolean isNewUser;

    private OAuthSigninResponse(String nickname, Boolean isNewUser) {
        this.nickname = nickname;
        this.isNewUser = isNewUser;
    }

    public static OAuthSigninResponse of(String nickname, Boolean isNewUser) {
        return new OAuthSigninResponse(nickname, isNewUser);
    }
}
