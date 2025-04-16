package com.example.picket.config;

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
    private String redirectUri;
    private String tokenUri;
    private String userInfoUri;
}
