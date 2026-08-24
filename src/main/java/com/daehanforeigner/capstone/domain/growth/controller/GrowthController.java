package com.daehanforeigner.capstone.domain.growth.controller;

import com.daehanforeigner.capstone.domain.growth.dto.GrowthTrendResponseDTO;
import com.daehanforeigner.capstone.domain.growth.dto.Period;
import com.daehanforeigner.capstone.domain.growth.service.GrowthService;
import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import com.daehanforeigner.capstone.global.rsdata.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stats/growth")
@RequiredArgsConstructor
@Tag(name = "통계", description = "학습 통계 · 성장 추이")
public class GrowthController {

    private final GrowthService growthService;

    // 통계 화면의 성장 그래프 진입 시 호출
    @Operation(
            summary = "정확도 성장 추이 조회",
            description = """
                    최근 N개 구간(주 또는 월)의 평균 발음 정확도를 오래된 순으로 조회합니다.

                    - `period` — `WEEK`(기본) 또는 `MONTH`
                    - `count` — 조회할 구간 수. 생략 시 주간 8, 월간 6. 1~52 사이여야 합니다.
                    - 기록이 없는 구간은 정확도 0으로 내려가되, `hasRecord`로 "기록 없음"과 "0점"을 구분할 수 있습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "`INVALID_DATE_PARAMETER` — period가 WEEK/MONTH가 아니거나 count가 1~52 범위를 벗어난 경우"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`")
    })
    @GetMapping
    public ResponseEntity<RsData<GrowthTrendResponseDTO>> getGrowthTrend(
            @AuthenticationPrincipal Long userId,
            @RequestParam(value = "period", required = false, defaultValue = "WEEK") String period,
            @RequestParam(value = "count", required = false) Integer count) {
        return ResponseEntity.ok(RsData.success(
                growthService.getTrend(userId, parsePeriod(period), count)));
    }

    // 문자열("week") → enum(WEEK) 변환. 유효하지 않은 값이면 400
    private Period parsePeriod(String period) {
        try {
            return Period.valueOf(period.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_DATE_PARAMETER);
        }
    }
}
