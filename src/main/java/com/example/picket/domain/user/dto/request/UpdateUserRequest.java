package com.example.picket.domain.user.dto.request;

import lombok.Getter;

@Getter
public class UpdateUserRequest {
    private String password;
    private String nickname;
    private String profileUrl;
}
