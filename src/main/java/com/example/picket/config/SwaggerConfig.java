package com.example.picket.config;


import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@SecurityScheme(
        type = SecuritySchemeType.APIKEY,
        name = "SESSIONID",
        description = "세션 ID 쿠키를 입력해주세요.",
        in = SecuritySchemeIn.COOKIE
)

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("SESSIONID",
                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.APIKEY)
                                        .in(io.swagger.v3.oas.models.security.SecurityScheme.In.COOKIE)
                                        .name("SESSIONID")
                                        .description("세션 ID를 쿠키에 입력해주세요.")
                        )
                )
                .info(apiInfo())
                .security(Collections.singletonList(new SecurityRequirement().addList("SESSIONID")));
    }

    @Bean
    public GroupedOpenApi groupedOpenApi() {
        String[] packages = {"com.example.picket"};
        return GroupedOpenApi.builder()
                .group("springdoc-openapi")
                .packagesToScan(packages)
                .build();
    }

    private Info apiInfo() {
        String description = "티켓팅 플랫폼을 구현해보는 프로젝트입니다.";
        return new Info()
                .title("Ticket을 Pick 하다.")
                .description(description);
    }
}