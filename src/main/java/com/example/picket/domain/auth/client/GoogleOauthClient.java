package com.example.picket.domain.auth.client;

import com.example.picket.common.dto.OauthUser;
import com.example.picket.common.enums.UserRole;
import com.example.picket.config.GoogleOAuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GoogleOauthClient {

    private final WebClient webClient;
    private final GoogleOAuthProperties googleOAuthProperties;

    public String getAccessToken(String code, UserRole userRole) {
        return webClient.post()
                .uri(googleOAuthProperties.getTokenUri())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .bodyValue("code="+code
                        +"&client_id="+googleOAuthProperties.getClientId()
                        +"&client_secret="+googleOAuthProperties.getClientSecret()
                        +"&redirect_uri="+googleOAuthProperties.getRedirectUriByRole(userRole)
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
}
