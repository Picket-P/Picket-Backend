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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
@Tag(name = "유저 관리 API", description = "유저 프로필 조회, 프로필 수정, 비밀번호 수정, 유저 탈퇴 기능 API입니다.")
public class UserController {

    private final UserCommandService userService;
    private final UserQueryService userQueryService;

    @Operation(summary = "유저 프로필 조회", description = "유저 프로필 조회 API입니다")
    @GetMapping("/users")
    public ResponseEntity<UserResponse> getUser(@Auth AuthUser authUser) {
        return ResponseEntity.ok(userQueryService.getUserResponse(authUser));
    }

    @Operation(summary = "유저 프로필 수정", description = "유저 프로필 수정 API입니다")
    @PutMapping("/users")
    public ResponseEntity<UserResponse> updateUser(@Auth AuthUser authUser,
                                                   @RequestBody UpdateUserRequest request) {
        User updatedUser = userService.updateUser(authUser, request);
        return ResponseEntity.ok(UserResponse.of(
                updatedUser.getEmail(),
                updatedUser.getUserRole(),
                updatedUser.getProfileUrl(),
                updatedUser.getNickname(),
                updatedUser.getBirth(),
                updatedUser.getGender()
        ));
    }

    @Operation(summary = "유저 비밀번호 수정", description = "유저 비밀번호 수정 API입니다")
    @PutMapping("/users/password")
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

    @Operation(summary = "유저 프로필 이미지 업로드", description = "유저 프로필 이미지 업로드 API입니다")
    @PostMapping("/users/uploadImage")
    public ResponseEntity<String> uploadImage(
            HttpServletRequest request,
            @Parameter(description = "파일의 크기", required = true, example = "1024")
            @RequestHeader("Content-Length") long contentLength,
            @Parameter(description = "요청 이미지의 타입", required = true, example = "image/jpeg")
            @RequestHeader(value = "Content-Type", defaultValue = "application/octet-stream") String contentType) {
        return ResponseEntity.ok(userService.uploadImage(request, contentLength, contentType));
    }
}
