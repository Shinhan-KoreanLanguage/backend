package com.daehanforeigner.capstone.domain.phoneme_score.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "음소별 정확도")
public record PhonemeAccuracyResponseDTO(
        @Schema(description = "음소·음절 텍스트", example = "ㄹ")
        String phoneme,

        @Schema(description = "평균 점수", example = "62.3")
        double averageScore,

        @Schema(description = "이 음소를 시도한 총 횟수", example = "14")
        long attemptCount,

        @Schema(description = "취약(isWeak) 판정을 받은 횟수", example = "9")
        long weakCount
) {
}
