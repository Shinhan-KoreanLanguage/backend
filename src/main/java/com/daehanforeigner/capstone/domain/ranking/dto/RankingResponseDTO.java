package com.daehanforeigner.capstone.domain.ranking.dto;

import com.daehanforeigner.capstone.domain.ranking.entity.Ranking;
import io.swagger.v3.oas.annotations.media.Schema;

// TOP10 랭킹 리스트의 항목 하나
@Schema(description = "랭킹 항목")
public record RankingResponseDTO(
        @Schema(description = "순위 (1~10)", example = "1")
        int rank,

        @Schema(description = "닉네임", example = "김한국")
        String nickname,

        @Schema(description = "역대 최고 기록 (단어 개수)", example = "27")
        int bestScore) {

    public static RankingResponseDTO of(int rank, Ranking ranking) {
        return new RankingResponseDTO(rank, ranking.getUser().getNickname(), ranking.getBestScore());
    }
}
