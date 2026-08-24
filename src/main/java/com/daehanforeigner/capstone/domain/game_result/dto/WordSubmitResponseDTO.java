package com.daehanforeigner.capstone.domain.game_result.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// 단어 하나 제출 결과 — 프론트가 즉시 O/X 피드백을 보여주는 데 사용
@Schema(description = "단어 발음 제출 결과")
public record WordSubmitResponseDTO(
        @Schema(description = "정확하게 발음했는지 여부 — true면 카운트에 반영됩니다", example = "true")
        boolean isCorrect,

        @Schema(description = "AI 분석 정확도 원점수 (0~100)", example = "82.5")
        Double accuracy) {
}
