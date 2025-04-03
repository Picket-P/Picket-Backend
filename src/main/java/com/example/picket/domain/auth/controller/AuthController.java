package com.example.picket.domain.auth.controller;

import com.example.picket.common.enums.UserRole;
import com.example.picket.domain.auth.dto.request.SigninRequest;
import com.example.picket.domain.auth.dto.request.SignupRequest;
import com.example.picket.domain.auth.dto.response.SigninResponse;
import com.example.picket.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/signup/user")
    public ResponseEntity<Void> signupUser(@Valid @RequestBody SignupRequest request) {
        authService.signup(request, UserRole.USER);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/auth/signup/director")
    public ResponseEntity<Void> signupDirector(@Valid @RequestBody SignupRequest request) {
        authService.signup(request, UserRole.DIRECTOR);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/auth/signup/admin")
    public ResponseEntity<Void> signupAdmin(@Valid @RequestBody SignupRequest request) {
        authService.signup(request, UserRole.ADMIN);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/auth/signin")
    public ResponseEntity<SigninResponse> signin(HttpSession session, @Valid @RequestBody SigninRequest request) {
        SigninResponse response = authService.signin(session, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/signout")
    public ResponseEntity<Void> signout(HttpSession session) {
        authService.signout(session);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
