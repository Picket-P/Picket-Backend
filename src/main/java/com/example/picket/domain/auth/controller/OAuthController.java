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
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class OAuthController {

    private final OAuthService oAuthService;
    private final UserQueryService userQueryService;

    @GetMapping("/auth/session")
    public ResponseEntity<SessionResponse> getSession(@Auth AuthUser auth) {
        if (auth == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User user = userQueryService.getUser(auth.getId());
        return ResponseEntity.ok(SessionResponse.of(user.getNickname(), user.getEmail(), user.getUserRole()));
    }

    @GetMapping("/auth/callback/user")
    public ResponseEntity<OAuthSigninResponse> googleUserSignin(@RequestParam("code") String code, HttpSession session) {

        User user = oAuthService.getOrCreateUser(session, code, UserRole.USER);
        return ResponseEntity.ok(OAuthSigninResponse.of(user.getNickname(), user.getNickname() == null));
    }

    @GetMapping("/auth/callback/admin")
    public ResponseEntity<OAuthSigninResponse> googleAdminSignin(@RequestParam("code") String code, HttpSession session) {

        User user = oAuthService.getOrCreateUser(session, code, UserRole.ADMIN);
        return ResponseEntity.ok(OAuthSigninResponse.of(user.getNickname(), user.getNickname() == null));
    }

    @GetMapping("/auth/callback/director")
    public ResponseEntity<OAuthSigninResponse> googleDirectorSignin(@RequestParam("code") String code, HttpSession session) {

        User user = oAuthService.getOrCreateUser(session, code, UserRole.DIRECTOR);
        return ResponseEntity.ok(OAuthSigninResponse.of(user.getNickname(), user.getNickname() == null));
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<Void> googleSignup(@Auth AuthUser authUser, @Valid @RequestBody OAuthSignupRequest request) {
        oAuthService.signup(authUser.getId(), request.getNickname(), request.getBirth(), request.getGender());
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
