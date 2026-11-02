package com.daehanforeigner.capstone.domain.phoneme_score.repository;

import com.daehanforeigner.capstone.domain.phoneme_score.entity.PhonemeScore;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.entity.PronunciationAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhonemeScoreRepository extends JpaRepository<PhonemeScore, Long> {

    // 발음 시도에 대한 모든 음소 점수 조회
    List<PhonemeScore> findAllByAttempt(PronunciationAttempt attempt);

    // 콘텐츠·시도 삭제 시 함께 지워야 할 음소 점수 (attempt_id가 pronunciation_attempt를 참조)
    void deleteAllByAttemptIn(List<PronunciationAttempt> attempts);

    // 음소별 평균 점수·시도 횟수·취약 판정 횟수 — 취약 판정 많은 순, 평균 점수 낮은 순으로 정렬
    // [phoneme, averageScore, attemptCount, weakCount]
    @Query("SELECT ps.phoneme, AVG(ps.score), COUNT(ps), " +
            "SUM(CASE WHEN ps.isWeak = true THEN 1 ELSE 0 END) " +
            "FROM PhonemeScore ps " +
            "WHERE ps.attempt.user.userId = :userId " +
            "GROUP BY ps.phoneme " +
            "ORDER BY SUM(CASE WHEN ps.isWeak = true THEN 1 ELSE 0 END) DESC, AVG(ps.score) ASC")
    List<Object[]> findPhonemeStatsByUser(@Param("userId") Long userId);
}
