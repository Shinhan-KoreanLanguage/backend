package com.daehanforeigner.capstone.domain.feedback.repository;

import com.daehanforeigner.capstone.domain.feedback.entity.Feedback;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.entity.PronunciationAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // Attempt에 대한 Feedback 조회
    List<Feedback> findAllByAttempt(PronunciationAttempt attempt);

    // 콘텐츠·시도 삭제 시 함께 지워야 할 피드백 (attempt_id가 pronunciation_attempt를 참조)
    void deleteAllByAttemptIn(List<PronunciationAttempt> attempts);
}
