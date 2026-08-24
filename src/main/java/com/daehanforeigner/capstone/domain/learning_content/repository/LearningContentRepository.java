package com.daehanforeigner.capstone.domain.learning_content.repository;

import com.daehanforeigner.capstone.domain.content_category.entity.CategoryType;
import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningContentRepository extends JpaRepository<LearningContent, Long> {

    // 관리자 목록 조회: 유형·난이도·검색어 필터(null이면 무시) + 페이징
    @Query("SELECT c FROM LearningContent c " +
            "WHERE (:categoryId IS NULL OR c.contentCategory.categoryId = :categoryId) " +
            "AND (:contentType IS NULL OR c.contentType = :contentType) " +
            "AND (:difficulty IS NULL OR c.difficulty = :difficulty) " +
            "AND (:keyword IS NULL " +
            "     OR c.text LIKE CONCAT('%', :keyword, '%') " +
            "     OR c.pronunciationGuide LIKE CONCAT('%', :keyword, '%'))")
    Page<LearningContent> searchContents(@Param("categoryId") Long categoryId,
                                         @Param("contentType") ContentType contentType,
                                         @Param("difficulty") Difficulty difficulty,
                                         @Param("keyword") String keyword,
                                         Pageable pageable);

    // 회원용 목록 조회: 카테고리·유형·난이도·학습 상태 필터(null이면 무시)
    @Query("SELECT c FROM LearningContent c " +
            "WHERE (:categoryType IS NULL OR c.contentCategory.type = :categoryType) " +
            "AND (:categoryId IS NULL OR c.contentCategory.categoryId = :categoryId) " +
            "AND (:contentType IS NULL OR c.contentType = :contentType) " +
            "AND (:difficulty IS NULL OR c.difficulty = :difficulty) " +
            // 세 상태는 서로 겹치지 않아야 필터별 개수의 합이 전체와 같아진다.
            // 통과 이력이 우선이고, 그다음이 미해결 오답, 둘 다 아니면 미학습이다 (배지 판정과 동일한 순서)
            "AND (:status IS NULL " +
            "     OR (:status = 'COMPLETED' AND EXISTS " +
            "         (SELECT 1 FROM PronunciationAttempt a " +
            "          WHERE a.learningContent = c AND a.user.userId = :userId AND a.isPassed = true)) " +
            "     OR (:status = 'WRONG' " +
            "         AND NOT EXISTS " +
            "         (SELECT 1 FROM PronunciationAttempt a " +
            "          WHERE a.learningContent = c AND a.user.userId = :userId AND a.isPassed = true) " +
            "         AND EXISTS " +
            "         (SELECT 1 FROM WrongAnswer w " +
            "          WHERE w.learningContent = c AND w.user.userId = :userId AND w.isSolved = false)) " +
            "     OR (:status = 'NOT_STARTED' " +
            "         AND NOT EXISTS " +
            "         (SELECT 1 FROM PronunciationAttempt a " +
            "          WHERE a.learningContent = c AND a.user.userId = :userId AND a.isPassed = true) " +
            "         AND NOT EXISTS " +
            "         (SELECT 1 FROM WrongAnswer w " +
            "          WHERE w.learningContent = c AND w.user.userId = :userId AND w.isSolved = false)))")
    Page<LearningContent> searchForUser(@Param("userId") Long userId,
                                        @Param("categoryType") CategoryType categoryType,
                                        @Param("categoryId") Long categoryId,
                                        @Param("contentType") ContentType contentType,
                                        @Param("difficulty") Difficulty difficulty,
                                        @Param("status") String status,
                                        Pageable pageable);

    // 카테고리별 전체 콘텐츠 수 — 학습 완료율 계산용 (분모)
    @Query("SELECT c.contentCategory.categoryId, c.contentCategory.name, COUNT(c) " +
            "FROM LearningContent c GROUP BY c.contentCategory.categoryId, c.contentCategory.name")
    List<Object[]> countGroupByCategory();
}