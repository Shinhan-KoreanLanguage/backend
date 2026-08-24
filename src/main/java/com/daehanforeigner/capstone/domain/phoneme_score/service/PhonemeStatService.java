package com.daehanforeigner.capstone.domain.phoneme_score.service;

import com.daehanforeigner.capstone.domain.phoneme_score.dto.PhonemeAccuracyResponseDTO;
import com.daehanforeigner.capstone.domain.phoneme_score.repository.PhonemeScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 발음(음소)별 취약점 분석 담당
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhonemeStatService {

    private final PhonemeScoreRepository phonemeScoreRepository;

    // 취약 판정 많은 순, 평균 점수 낮은 순으로 음소별 통계를 조회
    public List<PhonemeAccuracyResponseDTO> getPhonemeAccuracy(Long userId) {
        return phonemeScoreRepository.findPhonemeStatsByUser(userId).stream()
                .map(row -> new PhonemeAccuracyResponseDTO(
                        (String) row[0],
                        round(((Number) row[1]).doubleValue()),
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue()))
                .toList();
    }

    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
