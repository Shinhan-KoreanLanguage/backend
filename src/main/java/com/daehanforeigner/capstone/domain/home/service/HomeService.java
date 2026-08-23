package com.daehanforeigner.capstone.domain.home.service;

import com.daehanforeigner.capstone.domain.home.dto.HomeSummaryResponseDTO;
import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.repository.PronunciationAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// 홈 화면 학습 현황 담당
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 조회 전용
public class HomeService {

    // 화면의 "학습한 단어"에는 음절도 포함
    private static final List<ContentType> WORD_TYPES = List.of(ContentType.SYLLABLE, ContentType.WORD);

    private static final List<ContentType> SENTENCE_TYPES = List.of(ContentType.SENTENCE);

    private final PronunciationAttemptRepository pronunciationAttemptRepository;

    // 홈 화면 진입 시 호출
    public HomeSummaryResponseDTO getSummary(Long userId) {
        long learnedWordCount = pronunciationAttemptRepository
                .countDistinctContentsByType(userId, WORD_TYPES);
        long practicedSentenceCount = pronunciationAttemptRepository
                .countDistinctContentsByType(userId, SENTENCE_TYPES);

        // 여러 단어 및 문장을 학습해도 학습 일수는 하루만 측정
        List<LocalDate> studyDates = pronunciationAttemptRepository.findAttemptTimesDesc(userId)
                .stream()
                .map(LocalDateTime::toLocalDate)
                .distinct()
                .toList();

        // 월요일 0시를 기준으로 이번 주와 지난주를 나눔
        LocalDateTime thisWeekStart = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime nextWeekStart = thisWeekStart.plusWeeks(1);
        LocalDateTime lastWeekStart = thisWeekStart.minusWeeks(1);

        // 이번 주는 기록이 없어도 0으로 본다 ("아직 연습 안 함 = 0%"는 자연스럽다)
        double weeklyAccuracy = averageOrZero(
                pronunciationAttemptRepository.findAverageAccuracy(userId, thisWeekStart, nextWeekStart));

        // 지난주는 null을 그대로 살린다. 0으로 바꾸면 비교 대상이 없는데도
        // 이번 주 점수만큼 상승한 것으로 표시되어 신규 회원에게 잘못된 정보가 된다
        Double lastWeekAccuracy =
                pronunciationAttemptRepository.findAverageAccuracy(userId, lastWeekStart, thisWeekStart);

        return new HomeSummaryResponseDTO(
                learnedWordCount,
                practicedSentenceCount,
                studyDates.size(),
                calculateStreak(studyDates),
                round(weeklyAccuracy),
                lastWeekAccuracy != null ? round(weeklyAccuracy - lastWeekAccuracy) : null
        );
    }

    // 마지막 학습일부터 하루씩 거슬러 올라가며 끊기지 않은 날을 센다.
    // 목록이 최신순이므로 앞에서부터 하루 차이인지만 확인하면 된다
    private long calculateStreak(List<LocalDate> studyDatesDesc) {
        if (studyDatesDesc.isEmpty()) {
            return 0;
        }

        long streak = 1;
        LocalDate previous = studyDatesDesc.get(0);

        for (int i = 1; i < studyDatesDesc.size(); i++) {
            LocalDate current = studyDatesDesc.get(i);

            // 하루 차이가 아니면 연속이 끊긴 것이므로 즉시 종료
            if (!previous.minusDays(1).equals(current)) {
                break;
            }

            streak++;
            previous = current;
        }

        return streak;
    }

    // 해당 기간에 기록이 없으면 AVG가 null을 반환하므로 0으로 바꾼다 (신규 회원 NPE 방지)
    private double averageOrZero(Double average) {
        return average != null ? average : 0.0;
    }

    // 화면에 소수점이 길게 노출되지 않도록 소수 첫째 자리까지만 남긴다
    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
