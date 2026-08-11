package com.daehanforeigner.capstone.domain.audio_file.repository;

import com.daehanforeigner.capstone.domain.audio_file.entity.AudioFile;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.entity.PronunciationAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AudioFileRepository extends JpaRepository<AudioFile, Long> {

    // 결과 조회 시 Attempt에 대한 AudioFile 조회
    Optional<AudioFile> findByAttempt(PronunciationAttempt attempt);
}
