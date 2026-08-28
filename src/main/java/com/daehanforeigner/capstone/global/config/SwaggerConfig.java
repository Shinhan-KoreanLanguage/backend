package com.daehanforeigner.capstone.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Swagger(OpenAPI) 전역 설정.
// 프론트가 백엔드 코드를 열지 않고도 연동할 수 있도록 공통 규칙을 여기에 적어둔다.
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "JWT";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                // 모든 API에 기본으로 JWT 인증을 걸어두고,
                // 인증이 필요 없는 API만 컨트롤러에서 @Operation(security = {})로 해제한다
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
                        모든 응답은 아래 형태로 감싸집니다. 실제 데이터는 `data` 안에 있습니다.
                        ```json
                        {
                          "success": true,
                          "data": { ... },
                          "error": null,
                          "timestamp": "2026-08-11T10:00:00"
                        }
                        ```
                        실패 시 `data`는 null이고, `error.code`로 원인을 구분합니다.
                        `error.message`는 사용자에게 그대로 보여줘도 되는 문구입니다.
                        ```json
                        {
                          "success": false,
                          "data": null,
                          "error": { "code": "USER_NOT_FOUND", "message": "존재하지 않는 회원입니다." },
                          "timestamp": "2026-08-11T10:00:00"
                        }
                        ```

                        ## 인증
                        로그인 응답의 `accessToken`을 우측 상단 **Authorize** 버튼에 입력하면
                        이후 요청 헤더에 자동으로 붙습니다. (`Bearer ` 접두사는 자동으로 붙으니 토큰만 넣으세요)

                        액세스 토큰은 60분, 리프레시 토큰은 30일간 유효합니다.
                        401 `TOKEN_EXPIRED`를 받으면 `POST /api/v1/auth/reissue`로 재발급 후 재시도하세요.

                        ## 회원 모국어에 따라 응답이 달라집니다
                        학습 콘텐츠의 **뜻·발음 가이드·모국어 발음 표기**는 회원가입 시 선택한 모국어로 내려갑니다.
                        **같은 콘텐츠라도 로그인한 회원에 따라 값이 다릅니다.** 별도 파라미터는 필요 없습니다.

                        | 회원 모국어 | `meaning` 예시 |
                        |---|---|
                        | KR | 물건을 넣어 들고 다니는 도구 |
                        | EN | bag |
                        | JP | かばん |
                        | CN | 包 |

                        모국어 번역이 없으면 **영어로 대체**하고, 영어도 없으면 `null`입니다.
                        상세 조회는 `translationLanguage`로 실제 내려간 언어를 확인할 수 있습니다.

                        ## 공통 에러
                        | 상태 | 코드 | 설명 |
                        |---|---|---|
                        | 400 | BAD_REQUEST | 형식·파라미터 오류 (검증 실패 포함) |
                        | 401 | TOKEN_EXPIRED / TOKEN_INVALID | 토큰 만료·위조 |
                        | 403 | FORBIDDEN | 권한 없음 (일반 사용자가 관리자 API 호출) |
                        | 404 | NOT_FOUND | 대상 리소스 없음 (경로 오타 포함) |
                        | 405 | METHOD_NOT_ALLOWED | 경로는 맞지만 메서드가 다름 (예: 조회 전용 경로에 PUT) |
                        | 500 | INTERNAL_SERVER_ERROR | 서버 내부 오류 |

                        > `404`·`405`는 대부분 **호출한 URL이 잘못된 경우**입니다.
                        > 관리자 API는 경로에 `/admin`이 들어갑니다 (`/api/v1/admin/contents/{id}`).
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
