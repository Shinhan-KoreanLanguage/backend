package com.daehanforeigner.capstone.domain.pronunciation_attempt.repository;

import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.entity.PronunciationAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
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

    // 학습한 단어·문장 수. 여러 번 시도해서 성공해도 한 번만 카운트하기 위해 DISTINCT를 사용
    @Query("SELECT COUNT(DISTINCT a.learningContent.contentId) FROM PronunciationAttempt a " +
            "WHERE a.user.userId = :userId AND a.learningContent.contentType IN :contentTypes")
    long countDistinctContentsByType(@Param("userId") Long userId,
                                     @Param("contentTypes") Collection<ContentType> contentTypes);

    // 학습일수·연속 학습 계산용
    @Query("SELECT a.createdAt FROM PronunciationAttempt a " +
            "WHERE a.user.userId = :userId ORDER BY a.createdAt DESC")
    List<LocalDateTime> findAttemptTimesDesc(@Param("userId") Long userId);

    // 기간별 평균 정확도. 해당 기간에 기록이 없으면 null이 반환
    @Query("SELECT AVG(a.accuracy) FROM PronunciationAttempt a " +
            "WHERE a.user.userId = :userId AND a.createdAt >= :from AND a.createdAt < :to")
    Double findAverageAccuracy(@Param("userId") Long userId,
                               @Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to);

    // 특정 기간의 발음 시도 시각 — 출석부(월별 캘린더) 조회용
    @Query("SELECT a.createdAt FROM PronunciationAttempt a " +
            "WHERE a.user.userId = :userId AND a.createdAt >= :from AND a.createdAt < :to")
    List<LocalDateTime> findAttemptTimesBetween(@Param("userId") Long userId,
                                                @Param("from") LocalDateTime from,
                                                @Param("to") LocalDateTime to);

    // 카테고리별 합격한(서로 다른) 콘텐츠 수 — 학습 완료율 계산용 (분자)
    @Query("SELECT a.learningContent.contentCategory.categoryId, COUNT(DISTINCT a.learningContent.contentId) " +
            "FROM PronunciationAttempt a " +
            "WHERE a.user.userId = :userId AND a.isPassed = true " +
            "GROUP BY a.learningContent.contentCategory.categoryId")
    List<Object[]> countDistinctPassedContentsGroupByCategory(@Param("userId") Long userId);
}
