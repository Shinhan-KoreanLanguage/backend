package com.daehanforeigner.capstone.domain.audio_file.entity;

import com.daehanforeigner.capstone.domain.pronunciation_attempt.PronunciationAttempt;
import com.daehanforeigner.capstone.global.entity.GlobalEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "audio_file")
public class AudioFile extends GlobalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audio_file_id")
    private Long audioFileId;

    @ManyToOne
    @JoinColumn(name = "attempt_id")
    private PronunciationAttempt attempt;

    @Column(name = "file_url") // 오디오 파일 URL
    private String fileUrl;

    @Column(name = "file_size") // 오디오 파일 크기
    private int fileSize;

    @Column(name = "duration_ms") // 오디오 파일 길이 (밀리초 단위)
    private int durationMs;
}
