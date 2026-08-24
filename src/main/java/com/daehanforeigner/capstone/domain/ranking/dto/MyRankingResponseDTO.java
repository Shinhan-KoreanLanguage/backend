package com.daehanforeigner.capstone.domain.ranking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// 내 랭킹 조회 응답
@Schema(description = "개인 랭킹 정보")
public record MyRankingResponseDTO(
        @Schema(description = "게임 기록이 있는지 여부 — false면 rank·bestScore는 의미 없는 기본값입니다", example = "true")
        boolean hasRecord,

        @Schema(description = "현재 전체 순위 (1부터 시작)", example = "5")
        int rank,

        @Schema(description = "역대 최고 기록 (단어 개수)", example = "19")
        int bestScore,

        @Schema(description = "지금까지 모든 게임에서 말한 누적 단어 수", example = "142")
        int spokenWordCount) {

    public static MyRankingResponseDTO noRecord() {
        return new MyRankingResponseDTO(false, 0, 0, 0);
    }
}
