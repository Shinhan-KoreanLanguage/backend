package com.daehanforeigner.capstone.domain.wrong_answer.repository;

import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.user.entity.User;
import com.daehanforeigner.capstone.domain.wrong_answer.entity.WrongAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WrongAnswerRepository extends JpaRepository<WrongAnswer, Long> {

    // 사용자와 학습 콘텐츠에 해당하는 오답 기록 조회
    Optional<WrongAnswer> findByUserAndLearningContent(User user, LearningContent learningContent);
}