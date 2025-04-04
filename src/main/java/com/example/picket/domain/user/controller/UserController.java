package com.example.picket.domain.user.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.domain.user.dto.request.UpdatePasswordRequest;
import com.example.picket.domain.user.dto.request.UpdateUserRequest;
import com.example.picket.domain.user.dto.request.WithdrawUserRequest;
import com.example.picket.domain.user.dto.response.UserResponse;
import com.example.picket.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/api/v1/users")
    public ResponseEntity<UserResponse> getUser(@Auth AuthUser authUser) {
        return ResponseEntity.ok(userService.getUser(authUser));
    }

    @PatchMapping("/api/v1/users")
    public ResponseEntity<UserResponse> updateUser(@Auth AuthUser authUser,
                                                   @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(authUser, request));
    }

    @PatchMapping("/api/v1/users/password")
    public void updatePassword(@Auth AuthUser authUser,
                               @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(authUser, request);
    }

    @DeleteMapping("/api/v1/users/withdraw")
    public void withdrawUser(@Auth AuthUser authUser,
                             @RequestBody WithdrawUserRequest request) {
        userService.withdrawUserRequest(authUser, request);
    }
}
