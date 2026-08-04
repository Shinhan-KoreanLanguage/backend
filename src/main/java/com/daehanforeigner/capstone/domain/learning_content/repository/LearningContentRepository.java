package com.daehanforeigner.capstone.domain.learning_content.repository;

import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LearningContentRepository extends JpaRepository<LearningContent, Long> {

    // 관리자 목록 조회: 유형·난이도·검색어 필터(null이면 무시) + 페이징
    @Query("SELECT c FROM LearningContent c " +
            "WHERE (:contentType IS NULL OR c.contentType = :contentType) " +
            "AND (:difficulty IS NULL OR c.difficulty = :difficulty) " +
            "AND (:keyword IS NULL " +
            "     OR c.text LIKE CONCAT('%', :keyword, '%') " +
            "     OR c.pronunciationGuide LIKE CONCAT('%', :keyword, '%'))")
    Page<LearningContent> searchContents(@Param("contentType") ContentType contentType,
                                         @Param("difficulty") Difficulty difficulty,
                                         @Param("keyword") String keyword,
                                         Pageable pageable);
}