package com.example.picket.domain.auth.controller;

import com.example.picket.common.enums.UserRole;
import com.example.picket.domain.auth.dto.request.SigninRequest;
import com.example.picket.domain.auth.dto.request.SignupRequest;
import com.example.picket.domain.auth.dto.request.VerifyCodeRequest;
import com.example.picket.domain.auth.dto.response.SigninResponse;
import com.example.picket.domain.auth.dto.response.SignupResponse;
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
@RequestMapping("/api/v2")
@Tag(name = "인증/인가 API", description = "회원가입, 로그인, 로그아웃 API입니다.")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입 정보 입력", description = "유저 역할을 가진 회원가입 정보를 입력합니다. 회원가입은 이메일 인증 후 완료됩니다.")
    @PostMapping("/auth/signup/user")
    public ResponseEntity<SignupResponse> signupUser(@Valid @RequestBody SignupRequest request) {
        String tempUserId = authService.registerUserInfo(request.getEmail(), request.getPassword(),
                request.getNickname(), request.getBirth(), request.getGender(), UserRole.USER);
        return ResponseEntity.ok(new SignupResponse(tempUserId, request.getEmail()));
    }

    @Operation(summary = "회원가입 정보 입력", description = "디렉터 역할을 가진 회원가입 정보를 입력합니다. 회원가입은 이메일 인증 후 완료됩니다.")
    @PostMapping("/auth/signup/director")
    public ResponseEntity<SignupResponse> signupDirector(@Valid @RequestBody SignupRequest request) {
        String tempUserId = authService.registerUserInfo(request.getEmail(), request.getPassword(),
                request.getNickname(), request.getBirth(), request.getGender(), UserRole.DIRECTOR);
        return ResponseEntity.ok(new SignupResponse(tempUserId, request.getEmail()));
    }

    @Operation(summary = "회원가입 정보 입력", description = "관리자 역할을 가진 회원가입 정보를 입력합니다. 회원가입은 이메일 인증 후 완료됩니다.")
    @PostMapping("/auth/signup/admin")
    public ResponseEntity<SignupResponse> signupAdmin(@Valid @RequestBody SignupRequest request) {
        String tempUserId = authService.registerUserInfo(request.getEmail(), request.getPassword(),
                request.getNickname(), request.getBirth(), request.getGender(), UserRole.ADMIN);
        return ResponseEntity.ok(new SignupResponse(tempUserId, request.getEmail()));
    }

    @Operation(summary = "이메일 인증 코드 확인", description = "발송된 이메일 인증 코드를 확인하고 회원가입을 완료합니다.")
    @PostMapping("/auth/verify-code")
    public ResponseEntity<Void> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        boolean isVerified = authService.verifyCodeAndCompleteSignup(request.getEmail(), request.getVerificationCode());
        if (isVerified) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "회원가입(이메일 인증 X)", description = "유저 역할을 가진 회원가입 정보를 입력합니다. 특정 이메일만 인증이 가능하므로, 회원가입의 경우 해당 API 사용을 권장합니다.")
    @PostMapping("/auth/signup/user/notemail")
    public ResponseEntity<SignupResponse> signupUserWithoutVerify(@Valid @RequestBody SignupRequest request) {
        authService.signup(request.getEmail(), request.getPassword(),
                request.getNickname(), request.getBirth(), request.getGender(), UserRole.USER);
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