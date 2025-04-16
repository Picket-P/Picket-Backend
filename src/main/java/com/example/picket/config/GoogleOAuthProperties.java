package com.example.picket.config;

import com.example.picket.common.enums.UserRole;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.google")
@Getter
public class GoogleOAuthProperties {
    private String clientId;
    private String clientSecret;
    private String tokenUri;
    private String userInfoUri;

    private RedirectUris redirectUris;

    public GoogleOAuthProperties(String clientId, String clientSecret, String tokenUri, String userInfoUri, RedirectUris redirectUris) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenUri = tokenUri;
        this.userInfoUri = userInfoUri;
        this.redirectUris = redirectUris;
    }

    @Getter
    public static class RedirectUris {
        private String user;
        private String director;
        private String admin;

        public RedirectUris(String user, String director, String admin) {
            this.user = user;
            this.director = director;
            this.admin = admin;
        }
    }


    public String getRedirectUriByRole(UserRole role) {
        return switch (role) {
            case ADMIN -> redirectUris.getAdmin();
            case DIRECTOR -> redirectUris.getDirector();
            default -> redirectUris.getUser();
        };
    }
}
