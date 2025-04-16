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
        if(auth == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User user = userQueryService.getUser(auth.getId());
        return ResponseEntity.ok(SessionResponse.toDto(user.getNickname(), user.getEmail(), user.getUserRole()));
    }

}
