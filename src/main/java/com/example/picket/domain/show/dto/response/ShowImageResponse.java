package com.example.picket.domain.show.dto.response;

import lombok.Getter;

@Getter
public class ShowImageResponse {

    private final String profileUrl;

    private ShowImageResponse(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public static ShowImageResponse of(String profileUrl) {
        return new ShowImageResponse(profileUrl);
    }
}
