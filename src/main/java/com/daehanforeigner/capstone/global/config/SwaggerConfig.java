package com.daehanforeigner.capstone.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "JWT";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                // 모든 API에 기본으로 JWT 인증을 걸어두고, 인증이 필요 없는 API만
                // 컨트롤러에서 @SecurityRequirements로 해제한다 (permitAll 대상)
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, jwtScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("한국어 발음 교정 서비스 API")
                .version("v1")
                .description("""
                        외국인 대상 한국어 발음 학습 서비스 백엔드 API입니다.

                        ## 응답 형식
                        모든 응답은 RsData로 감싸집니다. 실제 데이터는 `data` 안에 있습니다.
                        ```json
                        { "success": true, "data": { ... }, "error": null, "timestamp": "2026-08-11T10:00:00" }
                        ```
                        실패 시 `data`는 null이고 `error.code`로 원인을 구분합니다.
                        ```json
                        { "success": false, "data": null, "error": { "code": "USER_NOT_FOUND", "message": "존재하지 않는 회원입니다." }, "timestamp": "..." }
                        ```

                        ## 인증
                        로그인 응답의 accessToken을 우측 상단 **Authorize** 버튼에 입력하면
                        이후 요청에 자동으로 붙습니다. (Bearer 접두사는 자동)

                        ## 공통 에러
                        - 401 `TOKEN_EXPIRED` / `TOKEN_INVALID` — 토큰 만료·위조. 재발급 후 재시도
                        - 403 `FORBIDDEN` — 권한 없음 (일반 사용자가 관리자 API 호출)
                        """);
    }

    // Authorize 버튼에 토큰만 넣으면 되도록 bearer 방식으로 설정
    private SecurityScheme jwtScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
    }
}