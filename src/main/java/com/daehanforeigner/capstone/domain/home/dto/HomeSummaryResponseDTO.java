package com.daehanforeigner.capstone.domain.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// 홈 화면 상단의 "나의 학습 현황" 4개 카드와 "발음 정확도" 영역에 필요한 값.
// 화면 하나를 채우는 데 여러 번 호출하지 않도록 한 번에 담아 내려준다
@Schema(description = "홈 대시보드 학습 현황 요약")
public record HomeSummaryResponseDTO(
        @Schema(description = "학습한 단어 수 — 시도한 적 있는 서로 다른 음절·단어 콘텐츠 수", example = "678")
        long learnedWordCount,

        @Schema(description = "연습한 문장 수 — 시도한 적 있는 서로 다른 문장 콘텐츠 수", example = "256")
        long practicedSentenceCount,

        @Schema(description = "학습일수 — 한 번이라도 연습한 날의 수", example = "12")
        long studyDayCount,

        @Schema(description = "연속 학습일수 — 마지막 학습일부터 하루도 빠지지 않고 이어진 일수", example = "7")
        long streakDayCount,

        @Schema(description = "이번 주 발음 정확도 평균(%) — 이번 주 기록이 없으면 0", example = "68.0")
        double weeklyAccuracy,

        @Schema(description = "지난주 대비 증감(%) — 양수면 상승. 지난주 기록이 없으면 0", example = "8.0")
        double accuracyDiff
) {
}
