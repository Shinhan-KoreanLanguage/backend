package com.daehanforeigner.capstone.domain.audio_file.repository;

import com.daehanforeigner.capstone.domain.audio_file.entity.AudioFile;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.entity.PronunciationAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AudioFileRepository extends JpaRepository<AudioFile, Long> {

    // 결과 조회 시 Attempt에 대한 AudioFile 조회
    Optional<AudioFile> findByAttempt(PronunciationAttempt attempt);

    // 콘텐츠·시도 삭제 시 함께 지워야 할 녹음 파일 레코드 (attempt_id가 pronunciation_attempt를 참조)
    void deleteAllByAttemptIn(List<PronunciationAttempt> attempts);
}
