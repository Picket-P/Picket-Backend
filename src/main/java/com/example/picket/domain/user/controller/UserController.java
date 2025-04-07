package com.example.picket.domain.user.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.domain.user.dto.request.UpdatePasswordRequest;
import com.example.picket.domain.user.dto.request.UpdateUserRequest;
import com.example.picket.domain.user.dto.request.WithdrawUserRequest;
import com.example.picket.domain.user.dto.response.UserResponse;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.service.UserCommandService;
import com.example.picket.domain.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

    private final UserCommandService userService;
    private final UserQueryService userQueryService;

    @GetMapping("/users")
    public ResponseEntity<UserResponse> getUser(@Auth AuthUser authUser) {
        return ResponseEntity.ok(userQueryService.getUser(authUser));
    }

    @PatchMapping("/users")
    public ResponseEntity<UserResponse> updateUser(@Auth AuthUser authUser,
                                                   @RequestBody UpdateUserRequest request) {
        User updatedUser = userService.updateUser(authUser, request);
        return ResponseEntity.ok(UserResponse.toDto(
                updatedUser.getEmail(),
                updatedUser.getUserRole(),
                updatedUser.getProfileUrl(),
                updatedUser.getNickname(),
                updatedUser.getBirth(),
                updatedUser.getGender()
        ));
    }

    @PatchMapping("/users/password")
    public void updatePassword(@Auth AuthUser authUser,
                               @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(authUser, request);
    }

    @DeleteMapping("/users/withdraw")
    public void withdrawUser(@Auth AuthUser authUser,
                             @RequestBody WithdrawUserRequest request) {
        userService.withdrawUserRequest(authUser, request);
    }
}
