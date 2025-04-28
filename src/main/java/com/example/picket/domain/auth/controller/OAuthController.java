package com.example.picket.domain.auth.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.UserRole;
import com.example.picket.domain.auth.dto.request.OAuthSignupRequest;
import com.example.picket.domain.auth.dto.response.OAuthSigninResponse;
import com.example.picket.domain.auth.dto.response.SessionResponse;
import com.example.picket.domain.auth.service.OAuthService;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.service.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
@Tag(name = "Auth API", description = "인증 및 OAuth 관련 API")
public class OAuthController {

    private final OAuthService oAuthService;
    private final UserQueryService userQueryService;

    @Operation(summary = "세션 조회", description = "현재 로그인된 세션 정보를 조회합니다.")
    @GetMapping("/auth/session")
    public ResponseEntity<SessionResponse> getSession(@Auth AuthUser auth) {
        if (auth == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User user = userQueryService.getUser(auth.getId());
        return ResponseEntity.ok(SessionResponse.of(user.getNickname(), user.getEmail(), user.getUserRole()));
    }

    @Operation(
            summary = "구글 OAuth 로그인 (User)",
            description = "Google OAuth를 통해 일반 사용자(User)로 로그인합니다." +
                    "\n\n※ Swagger에서는 OAuth 인가 코드 발급 과정을 거칠 수 없어 테스트할 수 없습니다." +
                    " 브라우저를 통해 직접 로그인 플로우를 타야 정상적으로 code를 받을 수 있습니다."
    )
    @GetMapping("/auth/callback/user")
    public ResponseEntity<OAuthSigninResponse> googleUserSignin(@RequestParam("code") String code, HttpSession session) {

        User user = oAuthService.getOrCreateUser(session, code, UserRole.USER);
        return ResponseEntity.ok(OAuthSigninResponse.of(user.getNickname(), user.getNickname() == null));
    }

    @Operation(
            summary = "구글 OAuth 로그인 (Admin)",
            description = "Google OAuth를 통해 일반 관리자(Admin)로 로그인합니다." +
                    "\n\n※ Swagger에서는 OAuth 인가 코드 발급 과정을 거칠 수 없어 테스트할 수 없습니다." +
                    " 브라우저를 통해 직접 로그인 플로우를 타야 정상적으로 code를 받을 수 있습니다."
    )
    @GetMapping("/auth/callback/admin")
    public ResponseEntity<OAuthSigninResponse> googleAdminSignin(@RequestParam("code") String code, HttpSession session) {

        User user = oAuthService.getOrCreateUser(session, code, UserRole.ADMIN);
        return ResponseEntity.ok(OAuthSigninResponse.of(user.getNickname(), user.getNickname() == null));
    }

    @Operation(
            summary = "구글 OAuth 로그인 (Director)",
            description = "Google OAuth를 통해 감독자(Director)로 로그인합니다." +
                    "\n\n※ Swagger에서는 OAuth 인가 코드 발급 과정을 거칠 수 없어 테스트할 수 없습니다." +
                    " 브라우저를 통해 직접 로그인 플로우를 타야 정상적으로 code를 받을 수 있습니다."
    )
    @GetMapping("/auth/callback/director")
    public ResponseEntity<OAuthSigninResponse> googleDirectorSignin(@RequestParam("code") String code, HttpSession session) {

        User user = oAuthService.getOrCreateUser(session, code, UserRole.DIRECTOR);
        return ResponseEntity.ok(OAuthSigninResponse.of(user.getNickname(), user.getNickname() == null));
    }

    @Operation(summary = "구글 신규회원 가입", description = "Google OAuth 인증 후, 신규 사용자의 닉네임, 생년월일, 성별을 등록합니다.")
    @PostMapping("/auth/signup")
    public ResponseEntity<Void> googleSignup(@Auth AuthUser authUser, @Valid @RequestBody OAuthSignupRequest request) {
        oAuthService.signup(authUser.getId(), request.getNickname(), request.getBirth(), request.getGender());
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
