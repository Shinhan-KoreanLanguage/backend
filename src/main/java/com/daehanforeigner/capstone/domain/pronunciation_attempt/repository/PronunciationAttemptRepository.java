package com.daehanforeigner.capstone.domain.pronunciation_attempt.repository;

import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.entity.PronunciationAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PronunciationAttemptRepository extends JpaRepository<PronunciationAttempt, Long> {

    // 목록에 나온 콘텐츠 중 한 번이라도 시도한 것의 ID (목록 조회 시 N + 1 문제 해결)
    @Query("SELECT DISTINCT a.learningContent.contentId FROM PronunciationAttempt a " +
            "WHERE a.user.userId = :userId AND a.learningContent IN :contents")
    List<Long> findAttemptedContentIds(@Param("userId") Long userId,
                                       @Param("contents") List<LearningContent> contents);

    // 그중 통과한 적 있는 콘텐츠의 ID (학습완료 배지 판정에 사용)
    @Query("SELECT DISTINCT a.learningContent.contentId FROM PronunciationAttempt a " +
            "WHERE a.user.userId = :userId AND a.learningContent IN :contents AND a.isPassed = true")
    List<Long> findPassedContentIds(@Param("userId") Long userId,
                                    @Param("contents") List<LearningContent> contents);
}
