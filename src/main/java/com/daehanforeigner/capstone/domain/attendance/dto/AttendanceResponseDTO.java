package com.daehanforeigner.capstone.domain.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "월별 출석부")
public record AttendanceResponseDTO(
        @Schema(description = "조회한 연도", example = "2026")
        int year,

        @Schema(description = "조회한 월(1~12)", example = "8")
        int month,

        @Schema(description = "해당 월에 출석(발음 시도 1회 이상)한 날짜 목록", example = "[\"2026-08-01\", \"2026-08-03\"]")
        List<LocalDate> attendedDates,

        @Schema(description = "해당 월의 총 출석일수", example = "2")
        int attendedDayCount
) {
    public static AttendanceResponseDTO of(int year, int month, List<LocalDate> attendedDates) {
        return new AttendanceResponseDTO(year, month, attendedDates, attendedDates.size());
    }
}
