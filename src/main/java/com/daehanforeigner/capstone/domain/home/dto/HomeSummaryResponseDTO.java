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

        // 지난주 기록이 없을 때 0을 주면 "변화 없음"으로 읽혀 신규 회원이 상승한 것처럼 보인다.
        // "비교 대상 없음"은 null로 구분해 프론트가 '-' 등으로 표시할 수 있게 한다
        @Schema(description = "지난주 대비 증감(%) — 양수면 상승. **지난주 기록이 없으면 null**", example = "8.0",
                nullable = true)
        Double accuracyDiff
) {
}
