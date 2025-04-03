package com.example.picket.domain.user.controller;

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
    public ResponseEntity<UserResponse> getUser() {
        return ResponseEntity.ok(userService.getUser());
    }

    @PatchMapping("/api/v1/users")
    public ResponseEntity<UserResponse> updateUser(@RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(request));
    }
}
