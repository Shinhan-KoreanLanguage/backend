package com.daehanforeigner.capstone.domain.attendance.controller;

import com.daehanforeigner.capstone.domain.attendance.dto.AttendanceResponseDTO;
import com.daehanforeigner.capstone.domain.attendance.service.AttendanceService;
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
@RequestMapping("/api/v1/stats/attendance")
@RequiredArgsConstructor
@Tag(name = "통계", description = "학습 통계 · 출석부")
public class AttendanceController {

    private final AttendanceService attendanceService;

    // 출석부 캘린더 진입 시 호출
    @Operation(
            summary = "월별 출석부 조회",
            description = """
                    해당 월에 발음 시도가 1건 이상 있는 날짜 목록을 조회합니다. 합격 여부와 무관하게 시도만 있으면 출석으로 처리됩니다.

                    `year`, `month`를 모두 생략하면 이번 달 기준으로 조회합니다. 하나만 생략할 수는 없습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "`INVALID_DATE_PARAMETER` — year·month 중 하나만 주었거나 달력상 존재하지 않는 월인 경우"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`")
    })
    @GetMapping
    public ResponseEntity<RsData<AttendanceResponseDTO>> getAttendance(
            @AuthenticationPrincipal Long userId,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {
        return ResponseEntity.ok(RsData.success(attendanceService.getMonthlyAttendance(userId, year, month)));
    }
}
