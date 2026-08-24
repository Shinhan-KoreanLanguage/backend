package com.daehanforeigner.capstone.domain.growth.service;

import com.daehanforeigner.capstone.domain.growth.dto.GrowthTrendResponseDTO;
import com.daehanforeigner.capstone.domain.growth.dto.GrowthTrendResponseDTO.PeriodAccuracy;
import com.daehanforeigner.capstone.domain.growth.dto.Period;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.repository.PronunciationAttemptRepository;
import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

// 정확도 성장 추이(주간/월간) 담당
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GrowthService {

    private static final int DEFAULT_WEEK_COUNT = 8;
    private static final int DEFAULT_MONTH_COUNT = 6;
    private static final int MAX_COUNT = 52;

    private final PronunciationAttemptRepository pronunciationAttemptRepository;

    public GrowthTrendResponseDTO getTrend(Long userId, Period period, Integer count) {
        int resolvedCount = resolveCount(period, count);

        List<PeriodAccuracy> points = period == Period.WEEK
                ? buildWeeklyPoints(userId, resolvedCount)
                : buildMonthlyPoints(userId, resolvedCount);

        return new GrowthTrendResponseDTO(period, points, calculateGrowthRate(points));
    }

    // 이번 주(월요일 기준)부터 거슬러 올라가며 주별 평균 정확도를 조회
    private List<PeriodAccuracy> buildWeeklyPoints(Long userId, int count) {
        LocalDateTime thisWeekStart = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();

        List<PeriodAccuracy> points = new ArrayList<>();
        for (int weeksAgo = count - 1; weeksAgo >= 0; weeksAgo--) {
            LocalDateTime from = thisWeekStart.minusWeeks(weeksAgo);
            LocalDateTime to = from.plusWeeks(1);
            points.add(toPeriodAccuracy(from.toLocalDate(),
                    pronunciationAttemptRepository.findAverageAccuracy(userId, from, to)));
        }
        return points;
    }

    // 이번 달부터 거슬러 올라가며 월별 평균 정확도를 조회
    private List<PeriodAccuracy> buildMonthlyPoints(Long userId, int count) {
        YearMonth thisMonth = YearMonth.now();

        List<PeriodAccuracy> points = new ArrayList<>();
        for (int monthsAgo = count - 1; monthsAgo >= 0; monthsAgo--) {
            YearMonth month = thisMonth.minusMonths(monthsAgo);
            LocalDateTime from = month.atDay(1).atStartOfDay();
            LocalDateTime to = month.plusMonths(1).atDay(1).atStartOfDay();
            points.add(toPeriodAccuracy(from.toLocalDate(),
                    pronunciationAttemptRepository.findAverageAccuracy(userId, from, to)));
        }
        return points;
    }

    // 기록이 없는 구간은 0%로 표시하되, hasRecord로 "안 함"과 "0점"을 구분할 수 있게 한다
    private PeriodAccuracy toPeriodAccuracy(LocalDate periodStart, Double average) {
        return new PeriodAccuracy(periodStart, average != null ? round(average) : 0.0, average != null);
    }

    // 마지막 구간과 바로 이전 구간을 비교. 둘 중 하나라도 기록이 없으면 비교 대상이 없는 것이므로 null
    private Double calculateGrowthRate(List<PeriodAccuracy> points) {
        if (points.size() < 2) {
            return null;
        }
        PeriodAccuracy latest = points.get(points.size() - 1);
        PeriodAccuracy previous = points.get(points.size() - 2);

        if (!latest.hasRecord() || !previous.hasRecord()) {
            return null;
        }
        return round(latest.averageAccuracy() - previous.averageAccuracy());
    }

    private int resolveCount(Period period, Integer count) {
        if (count == null) {
            return period == Period.WEEK ? DEFAULT_WEEK_COUNT : DEFAULT_MONTH_COUNT;
        }
        if (count < 1 || count > MAX_COUNT) {
            throw new CustomException(ErrorCode.INVALID_DATE_PARAMETER);
        }
        return count;
    }

    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
