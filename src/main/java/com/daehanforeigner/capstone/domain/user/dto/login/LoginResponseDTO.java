package com.daehanforeigner.capstone.domain.user.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인·재발급 응답 — 두 토큰 모두 프론트에서 보관하세요")
public record LoginResponseDTO(
        @Schema(description = "액세스 토큰 (60분) — 이후 요청의 Authorization 헤더에 `Bearer {값}`으로 넣습니다",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.xxxxx")
        String accessToken,

        @Schema(description = "리프레시 토큰 (30일) — 액세스 토큰 만료 시 재발급에 사용합니다",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.yyyyy")
        String refreshToken
) {
}
