package com.example.picket.config;

import com.example.picket.common.enums.UserRole;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "oauth.google")
@Getter
@Setter
public class GoogleOAuthProperties {
    private String clientId;
    private String clientSecret;
    private String tokenUri;
    private String userInfoUri;

    private RedirectUris redirectUris;

    @Getter
    @Setter
    public static class RedirectUris {
        private String user;
        private String director;
        private String admin;
    }

    public String getRedirectUriByRole(UserRole role) {
        return switch (role) {
            case ADMIN -> redirectUris.getAdmin();
            case DIRECTOR -> redirectUris.getDirector();
            default -> redirectUris.getUser();
        };
    }
}
