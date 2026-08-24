package com.daehanforeigner.capstone.domain.growth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "정확도 성장 추이")
public record GrowthTrendResponseDTO(
        @Schema(description = "조회 단위", example = "WEEK")
        Period period,

        @Schema(description = "구간별 평균 정확도 목록 (오래된 구간이 먼저 옵니다)")
        List<PeriodAccuracy> points,

        @Schema(description = "마지막 구간과 그 이전 구간의 정확도 차이(%) — 양수면 상승. " +
                "둘 중 하나라도 기록이 없으면 null", example = "8.0", nullable = true)
        Double growthRate
) {
    @Schema(description = "구간별 평균 정확도")
    public record PeriodAccuracy(
            @Schema(description = "구간 시작일", example = "2026-08-03")
            LocalDate periodStart,

            @Schema(description = "구간 평균 정확도(%) — 기록이 없으면 0", example = "72.5")
            double averageAccuracy,

            @Schema(description = "해당 구간에 발음 시도 기록이 있었는지 여부", example = "true")
            boolean hasRecord
    ) {
    }
}
