package com.likelion.dermaday.common.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    public static final String COOKIE_AUTH_SCHEME = "cookieAuth";

    @Bean
    public OpenAPI dermadayOpenApi(@Value("${app.auth-cookie.name}") String authCookieName) {
        return new OpenAPI()
                .info(new Info()
                        .title("Dermaday API")
                        .version("v1")
                        .description("""
                                ## 인증 API 테스트 순서

                                1. [카카오 로그인](/oauth2/authorization/kakao) 또는
                                   [네이버 로그인](/oauth2/authorization/naver)을 진행합니다.
                                2. 로그인 성공 후 이 Swagger UI로 돌아옵니다.
                                3. `GET /api/v1/auth/csrf`를 먼저 실행합니다.
                                4. 이후 인증 및 CSRF 보호가 필요한 API를 실행합니다.

                                JWT는 HttpOnly 쿠키로 자동 전송되므로 Authorize 입력은 필요하지 않습니다.
                                """))
                .components(new Components()
                        .addSecuritySchemes(COOKIE_AUTH_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name(authCookieName)
                                .description("OAuth2 로그인 성공 시 발급되는 HttpOnly JWT 쿠키")));
    }
}
