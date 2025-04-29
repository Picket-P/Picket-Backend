package com.example.picket.domain.user.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.domain.user.dto.request.UpdatePasswordRequest;
import com.example.picket.domain.user.dto.request.UpdateUserRequest;
import com.example.picket.domain.user.dto.request.WithdrawUserRequest;
import com.example.picket.domain.user.dto.response.UserImageResponse;
import com.example.picket.domain.user.dto.response.UserResponse;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.service.UserCommandService;
import com.example.picket.domain.user.service.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @PostMapping(value = "/users/uploadImage", consumes = "multipart/form-data")
    public ResponseEntity<UserImageResponse> uploadImage(@Parameter(
            description = "업로드할 이미지 파일",
            required = true,
            name = "multipartFile",
            content = @Content(
                    mediaType = "multipart/form-data",
                    schema = @Schema(
                            type = "string",
                            format = "binary",
                            description = "지원 형식: JPEG, PNG, GIF, WEBP. 최대 크기: 8MB. 예: image.png",
                            example = "image.png"
                    )
            )
    ) @RequestParam("multipartFile") MultipartFile multipartFile) {
        return ResponseEntity.ok(UserImageResponse.of(userService.uploadImage(multipartFile)));
    }
}
