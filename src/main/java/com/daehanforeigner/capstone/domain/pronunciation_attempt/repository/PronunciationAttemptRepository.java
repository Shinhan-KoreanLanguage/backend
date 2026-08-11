package com.daehanforeigner.capstone.domain.pronunciation_attempt.repository;

import com.daehanforeigner.capstone.domain.pronunciation_attempt.entity.PronunciationAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PronunciationAttemptRepository extends JpaRepository<PronunciationAttempt, Long> {
}
