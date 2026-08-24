package com.daehanforeigner.capstone.domain.wrong_answer.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// 오답 노트 우측의 "오답 요약" 카드 4개와 "정확도 분포" 도넛에 필요한 값.
// 목록 상단 탭(전체 · 단어 · 문장)의 개수도 이 값을 그대로 쓰면 된다.
//
// 분포는 개수로 내려준다. 비율은 화면 표시 방식(소수점·반올림)에 따라 달라지므로
// 프론트에서 totalCount로 나눠 쓰는 편이 유연하다
@Schema(description = "오답 요약 및 정확도 분포")
public record WrongAnswerSummaryResponseDTO(
        @Schema(description = "총 오답 수", example = "24")
        long totalCount,

        @Schema(description = "단어 오답 수 — 음절 포함", example = "8")
        long wordCount,

        @Schema(description = "문장 오답 수", example = "16")
        long sentenceCount,

        @Schema(description = "복습 완료 수 — 재시도해 통과한 오답", example = "5")
        long solvedCount,

        @Schema(description = "정확도 80% 이상", example = "6")
        long accuracy80Plus,

        @Schema(description = "정확도 60~79%", example = "11")
        long accuracy60To79,

        @Schema(description = "정확도 40~59%", example = "5")
        long accuracy40To59,

        @Schema(description = "정확도 40% 미만", example = "2")
        long accuracyBelow40
) {
}
