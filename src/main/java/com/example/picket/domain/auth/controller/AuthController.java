package com.example.picket.domain.auth.controller;

import com.example.picket.common.enums.UserRole;
import com.example.picket.domain.auth.dto.request.SigninRequest;
import com.example.picket.domain.auth.dto.request.SignupRequest;
import com.example.picket.domain.auth.dto.response.SigninResponse;
import com.example.picket.domain.auth.service.AuthService;
import com.example.picket.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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
@Tag(name = "인증/인가 API", description = "회원가입, 로그인, 로그아웃 API입니다.")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "유저 역할을 가진 회원가입을 할 수 있습니다.")
    @PostMapping("/auth/signup/user")
    public ResponseEntity<Void> signupUser(@Valid @RequestBody SignupRequest request) {
        authService.signup(request.getEmail(), request.getPassword(), request.getNickname(), request.getBirth(),
                request.getGender(), UserRole.USER);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "회원가입", description = "디렉터 역할을 가진 회원가입을 할 수 있습니다.")
    @PostMapping("/auth/signup/director")
    public ResponseEntity<Void> signupDirector(@Valid @RequestBody SignupRequest request) {
        authService.signup(request.getEmail(), request.getPassword(), request.getNickname(), request.getBirth(),
                request.getGender(), UserRole.DIRECTOR);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "회원가입", description = "관리자 역할을 가진 회원가입을 할 수 있습니다.")
    @PostMapping("/auth/signup/admin")
    public ResponseEntity<Void> signupAdmin(@Valid @RequestBody SignupRequest request) {
        authService.signup(request.getEmail(), request.getPassword(), request.getNickname(), request.getBirth(),
                request.getGender(), UserRole.ADMIN);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "로그인", description = "로그인을 할 수 있습니다.")
    @PostMapping("/auth/signin")
    public ResponseEntity<SigninResponse> signin(HttpSession session, HttpServletResponse response,
                                                 @Valid @RequestBody SigninRequest request) {
        User user = authService.signin(session, response, request.getEmail(), request.getPassword());
        SigninResponse signinResponse = SigninResponse.of(user.getNickname());
        return ResponseEntity.ok(signinResponse);
    }

    @Operation(summary = "로그아웃", description = "로그인 되어있는 상태의 경우, 로그아웃을 할 수 있습니다.")
    @PostMapping("/auth/signout")
    public ResponseEntity<Void> signout(HttpSession session, HttpServletResponse response) {
        authService.signout(session, response);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
