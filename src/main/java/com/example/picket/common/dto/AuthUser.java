package com.example.picket.common.dto;

import com.example.picket.common.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthUser{
    private Long id;
    private UserRole userRole;

    @JsonCreator
    private AuthUser(@JsonProperty("id") Long id, @JsonProperty("userRole")UserRole userRole) {
        this.id = id;
        this.userRole = userRole;
    }

    public static AuthUser create(Long id, UserRole userRole) {
        return new AuthUser(id, userRole);
    }
}
