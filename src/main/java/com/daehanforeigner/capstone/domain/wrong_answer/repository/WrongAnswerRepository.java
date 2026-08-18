package com.daehanforeigner.capstone.domain.wrong_answer.repository;

import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.user.entity.User;
import com.daehanforeigner.capstone.domain.wrong_answer.entity.WrongAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
