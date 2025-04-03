package com.example.picket.common.dto;

import com.example.picket.common.enums.UserRole;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthUser {

    private Long id;
    private UserRole userRole;

    @Builder
    private AuthUser(Long id, UserRole userRole) {
        this.id = id;
        this.userRole = userRole;
    }
}
