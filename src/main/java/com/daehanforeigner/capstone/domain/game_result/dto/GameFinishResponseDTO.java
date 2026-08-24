package com.daehanforeigner.capstone.domain.game_result.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// 게임 종료 결과
@Schema(description = "발음 게임 종료 결과")
public record GameFinishResponseDTO(
        @Schema(description = "제한 시간 동안 정확히 발음한 단어 개수", example = "18")
        int score,

        @Schema(description = "역대 최고 기록을 경신했는지 여부", example = "true")
        boolean isNewBestScore) {
}
