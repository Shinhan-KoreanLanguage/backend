package com.daehanforeigner.capstone.domain.phoneme_score.repository;

import com.daehanforeigner.capstone.domain.phoneme_score.entity.PhonemeScore;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.entity.PronunciationAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhonemeScoreRepository extends JpaRepository<PhonemeScore, Long> {

    // 발음 시도에 대한 모든 음소 점수 조회
    List<PhonemeScore> findAllByAttempt(PronunciationAttempt attempt);
}
