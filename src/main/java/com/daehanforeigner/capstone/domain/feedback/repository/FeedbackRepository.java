package com.daehanforeigner.capstone.domain.feedback.repository;

import com.daehanforeigner.capstone.domain.feedback.entity.Feedback;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.entity.PronunciationAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // Attempt에 대한 Feedback 조회
    List<Feedback> findAllByAttempt(PronunciationAttempt attempt);
}
