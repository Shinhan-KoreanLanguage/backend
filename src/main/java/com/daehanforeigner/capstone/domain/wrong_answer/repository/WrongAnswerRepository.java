package com.daehanforeigner.capstone.domain.wrong_answer.repository;

import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.user.entity.User;
import com.daehanforeigner.capstone.domain.wrong_answer.dto.WrongAnswerSummaryResponseDTO;
import com.daehanforeigner.capstone.domain.wrong_answer.entity.WrongAnswer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WrongAnswerRepository extends JpaRepository<WrongAnswer, Long> {

    // 사용자와 학습 콘텐츠에 해당하는 오답 기록 조회
    Optional<WrongAnswer> findByUserAndLearningContent(User user, LearningContent learningContent);

    // 목록에 나온 콘텐츠 중 아직 해결하지 못한 오답의 ID (오답 배지 판정에 사용)
    @Query("SELECT w.learningContent.contentId FROM WrongAnswer w " +
            "WHERE w.user.userId = :userId AND w.learningContent IN :contents AND w.isSolved = false")
    List<Long> findUnsolvedContentIds(@Param("userId") Long userId,
                                      @Param("contents") List<LearningContent> contents);

    // 오답 노트 목록 조회.
    // 콘텐츠·카테고리를 함께 가져오지 않으면 행마다 추가 조회가 나가므로 fetch join으로 묶는다.
    // 카테고리는 nullable이라 LEFT로 걸어야 카테고리 없는 콘텐츠가 빠지지 않는다
    @Query(value = "SELECT w FROM WrongAnswer w " +
            "JOIN FETCH w.learningContent c " +
            "LEFT JOIN FETCH c.contentCategory cc " +
            "WHERE w.user.userId = :userId " +
            "AND c.contentType IN :contentTypes " +
            "AND (:solved IS NULL OR w.isSolved = :solved) " +
            "AND (:categoryId IS NULL OR cc.categoryId = :categoryId) " +
            "AND (:difficulty IS NULL OR c.difficulty = :difficulty) " +
            "AND (:keyword IS NULL " +
            "     OR c.text LIKE CONCAT('%', :keyword, '%') " +
            "     OR c.standardPronunciationText LIKE CONCAT('%', :keyword, '%'))",
            countQuery = "SELECT COUNT(w) FROM WrongAnswer w " +
                    "JOIN w.learningContent c " +
                    "LEFT JOIN c.contentCategory cc " +
                    "WHERE w.user.userId = :userId " +
                    "AND c.contentType IN :contentTypes " +
                    "AND (:solved IS NULL OR w.isSolved = :solved) " +
                    "AND (:categoryId IS NULL OR cc.categoryId = :categoryId) " +
                    "AND (:difficulty IS NULL OR c.difficulty = :difficulty) " +
                    "AND (:keyword IS NULL " +
                    "     OR c.text LIKE CONCAT('%', :keyword, '%') " +
                    "     OR c.standardPronunciationText LIKE CONCAT('%', :keyword, '%'))")
    Page<WrongAnswer> search(@Param("userId") Long userId,
                             @Param("contentTypes") Collection<ContentType> contentTypes,
                             @Param("solved") Boolean solved,
                             @Param("categoryId") Long categoryId,
                             @Param("difficulty") Difficulty difficulty,
                             @Param("keyword") String keyword,
                             Pageable pageable);

    // 오답 요약(카드 4종 + 정확도 분포)을 한 번의 쿼리로 만든다.
    // COUNT는 null을 세지 않으므로 CASE에 ELSE를 두지 않으면 조건에 맞는 것만 세어진다.
    // SUM 대신 COUNT를 쓰는 이유는 대상이 하나도 없을 때 null이 아니라 0이 나오기 때문
    @Query("SELECT new com.daehanforeigner.capstone.domain.wrong_answer.dto.WrongAnswerSummaryResponseDTO(" +
            "COUNT(w), " +
            "COUNT(CASE WHEN w.learningContent.contentType <> :sentenceType THEN 1 END), " +
            "COUNT(CASE WHEN w.learningContent.contentType = :sentenceType THEN 1 END), " +
            "COUNT(CASE WHEN w.isSolved = true THEN 1 END), " +
            "COUNT(CASE WHEN w.lastAccuracy >= 80 THEN 1 END), " +
            "COUNT(CASE WHEN w.lastAccuracy >= 60 AND w.lastAccuracy < 80 THEN 1 END), " +
            "COUNT(CASE WHEN w.lastAccuracy >= 40 AND w.lastAccuracy < 60 THEN 1 END), " +
            "COUNT(CASE WHEN w.lastAccuracy < 40 THEN 1 END)) " +
            "FROM WrongAnswer w WHERE w.user.userId = :userId")
    WrongAnswerSummaryResponseDTO getSummary(@Param("userId") Long userId,
                                             @Param("sentenceType") ContentType sentenceType);
}
