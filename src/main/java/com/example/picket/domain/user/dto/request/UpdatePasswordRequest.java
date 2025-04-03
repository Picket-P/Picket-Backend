package com.example.picket.domain.user.dto.request;

import lombok.Getter;

@Getter
public class UpdatePasswordRequest {

    private String password;
    private String newPassword;
}
