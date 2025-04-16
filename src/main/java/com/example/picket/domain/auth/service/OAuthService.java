package com.example.picket.domain.auth.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.dto.OauthUser;
import com.example.picket.common.enums.Gender;
import com.example.picket.common.enums.OAuth;
import com.example.picket.common.enums.UserRole;
import com.example.picket.config.GoogleOAuthProperties;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.repository.UserRepository;
import com.example.picket.domain.user.service.UserQueryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class OAuthService {

    private final GoogleOAuthProperties googleOAuthProperties;
    private final WebClient webClient;
    private final UserQueryService userQueryService;
    private final UserRepository userRepository;

    public String getAccessToken(String code, String redirectUrl) {
        return webClient.post()
                .uri(googleOAuthProperties.getTokenUri())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .bodyValue("code="+code
                        +"&client_id="+googleOAuthProperties.getClientId()
                        +"&client_secret="+googleOAuthProperties.getClientSecret()
                        +"&redirect_uri="+redirectUrl
                        +"&grant_type=authorization_code")
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String)response.get("access_token"))
                .block();
    }

    public OauthUser getUser(String accessToken){
        return webClient.get()
                .uri(googleOAuthProperties.getUserInfoUri())
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(OauthUser.class)
                .block();
    }


    @Transactional
    public User getOrCreateUser(HttpSession session, String code, UserRole userRole) {
        String accessToken = getAccessToken(code, googleOAuthProperties.getRedirectUriByRole(userRole));
        OauthUser oauthUser = getUser(accessToken);
        User user = userQueryService.getUserByEmail(oauthUser.getEmail())
                .orElseGet( () -> userRepository.save(
                        User.toOAuthEntity(oauthUser.getEmail(),
                                "",
                                userRole,
                                null,
                                null,
                                null,
                                null,
                                oauthUser.getId(),
                                OAuth.GOOGLE
                                )));



        AuthUser authUser = AuthUser.toEntity(user.getId(), user.getUserRole());
        session.setAttribute("authUser", authUser);
        return user;
    }

    @Transactional
    public void signup(Long id, String nickname, LocalDate birth, Gender gender, UserRole userRole) {
        User user = userQueryService.getUser(id);
        user.oAuthSignup(userRole, null, nickname, birth, gender);
    }

}
