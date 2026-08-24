package com.daehanforeigner.capstone.domain.attendance.service;

import com.daehanforeigner.capstone.domain.attendance.dto.AttendanceResponseDTO;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.repository.PronunciationAttemptRepository;
import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

// 출석부(월별 학습 날짜) 담당
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final PronunciationAttemptRepository pronunciationAttemptRepository;

    // 출석부 캘린더 진입 시 호출 — year·month를 둘 다 생략하면 이번 달 기준
    public AttendanceResponseDTO getMonthlyAttendance(Long userId, Integer year, Integer month) {
        YearMonth targetMonth = resolveYearMonth(year, month);

        LocalDateTime from = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime to = targetMonth.plusMonths(1).atDay(1).atStartOfDay();

        // 발음 시도가 1건 이상 있으면 출석 — 합격 여부는 무관
        List<LocalDate> attendedDates = pronunciationAttemptRepository
                .findAttemptTimesBetween(userId, from, to)
                .stream()
                .map(LocalDateTime::toLocalDate)
                .distinct()
                .sorted()
                .toList();

        return AttendanceResponseDTO.of(targetMonth.getYear(), targetMonth.getMonthValue(), attendedDates);
    }

    // year·month는 함께 주거나 함께 생략해야 한다 (하나만 주면 의도가 모호함)
    private YearMonth resolveYearMonth(Integer year, Integer month) {
        if (year == null && month == null) {
            return YearMonth.now();
        }
        if (year == null || month == null) {
            throw new CustomException(ErrorCode.INVALID_DATE_PARAMETER);
        }
        try {
            return YearMonth.of(year, month);
        } catch (DateTimeException e) {
            throw new CustomException(ErrorCode.INVALID_DATE_PARAMETER);
        }
    }
}
