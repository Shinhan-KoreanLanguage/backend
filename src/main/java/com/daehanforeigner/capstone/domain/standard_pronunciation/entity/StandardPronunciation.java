package com.daehanforeigner.capstone.domain.standard_pronunciation.entity;

import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.global.entity.GlobalEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "standard_pronunciation")
public class StandardPronunciation extends GlobalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pronunciation_id")
    private Long pronunciationId;

    @ManyToOne
    @JoinColumn(name = "content_id")
    private LearningContent learningContent;

    @Column(name = "native_pitch_data", columnDefinition = "json") // 피치 데이터
    private String nativePitchData;

    @Column(name = "lip_sequence", columnDefinition = "json") // 입술 움직임 시퀀스
    private String lipSequence;

    @Column(name = "answer_audio_url") // 정답 오디오 URL (음성 파일)
    private String answerAudioUrl;

    @Column(name = "answer_video_url") // 정답 비디오 URL (영상 파일)
    private String answerVideoUrl;

    // 음성 파일 업데이트
    public void updateAudioUrl(String answerAudioUrl) {
        this.answerAudioUrl = answerAudioUrl;
    }

    // 비디오 파일 업데이트
    public void updateVideoUrl(String answerVideoUrl) {
        this.answerVideoUrl = answerVideoUrl;
    }
}
