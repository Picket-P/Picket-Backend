package com.example.picket.domain.auth.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.dto.OAuthUser;
import com.example.picket.common.enums.Gender;
import com.example.picket.common.enums.OAuth;
import com.example.picket.common.enums.UserRole;
import com.example.picket.domain.auth.client.GoogleOauthClient;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.repository.UserRepository;
import com.example.picket.domain.user.service.UserQueryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class OAuthService {

    private final UserQueryService userQueryService;
    private final UserRepository userRepository;
    private final GoogleOauthClient googleOauthClient;

    @Transactional
    public User getOrCreateUser(HttpSession session, String code, UserRole userRole) {
        String accessToken = googleOauthClient.getAccessToken(code, userRole);
        OAuthUser oauthUser = googleOauthClient.getUser(accessToken);
        User user = userQueryService.getUserByEmail(oauthUser.getEmail())
                .orElseGet( () -> userRepository.save(
                        User.toOAuthEntity(oauthUser.getEmail(),
                                userRole,
                                oauthUser.getId(),
                                OAuth.GOOGLE
                                )));

        AuthUser authUser = AuthUser.toEntity(user.getId(), user.getUserRole());
        session.setAttribute("authUser", authUser);
        return user;
    }

    @Transactional
    public void signup(Long id, String nickname, LocalDate birth, Gender gender) {
        User user = userQueryService.getUser(id);
        user.oAuthSignup(nickname, birth, gender);
    }

}
