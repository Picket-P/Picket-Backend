package com.example.picket.domain.user.dto.response;

import lombok.Getter;

@Getter
public class UserImageResponse {

    private final String profileUrl;

    private UserImageResponse(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public static UserImageResponse of(String profileUrl) {
        return new UserImageResponse(profileUrl);
    }
}
