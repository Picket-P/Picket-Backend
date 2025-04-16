package com.example.picket.common.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthUser {
    private String id;
    private String email;

    private OAuthUser(String id, String email) {
        this.id = id;
        this.email = email;
    }

    public static OAuthUser toEntity(String id,  String email) {
        return new OAuthUser(id, email);
    }
}
