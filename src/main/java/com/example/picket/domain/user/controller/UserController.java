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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "유저 관리 API", description = "유저 프로필 조회, 프로필 수정, 비밀번호 수정, 유저 탈퇴 기능 API입니다.")
public class UserController {

    private final UserCommandService userService;
    private final UserQueryService userQueryService;

    @Operation(summary = "유저 프로필 조회", description = "유저 프로필 조회 API입니다")
    @GetMapping("/users")
    public ResponseEntity<UserResponse> getUser(@Auth AuthUser authUser) {
        return ResponseEntity.ok(userQueryService.getUser(authUser));
    }

    @Operation(summary = "유저 프로필 수정", description = "유저 프로필 수정 API입니다")
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

    @Operation(summary = "유저 비밀번호 수정", description = "유저 비밀번호 수정 API입니다")
    @PatchMapping("/users/password")
    public void updatePassword(@Auth AuthUser authUser,
                               @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(authUser, request);
    }

    @Operation(summary = "유저 탈퇴", description = "유저 탈퇴 API입니다")
    @DeleteMapping("/users/withdraw")
    public void withdrawUser(@Auth AuthUser authUser,
                             @RequestBody WithdrawUserRequest request) {
        userService.withdrawUserRequest(authUser, request);
    }
}
