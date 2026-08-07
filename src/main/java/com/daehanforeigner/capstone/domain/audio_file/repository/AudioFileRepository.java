package com.daehanforeigner.capstone.domain.audio_file.repository;

import com.daehanforeigner.capstone.domain.audio_file.entity.AudioFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AudioFileRepository extends JpaRepository<AudioFile, Long> {
}
