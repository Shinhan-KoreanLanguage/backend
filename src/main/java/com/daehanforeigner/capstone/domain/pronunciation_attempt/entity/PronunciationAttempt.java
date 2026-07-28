package com.daehanforeigner.capstone.domain.pronunciation_attempt.entity;

import com.daehanforeigner.capstone.domain.learning_content.LearningContent;
import com.daehanforeigner.capstone.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pronunciation_attempt")
public class PronunciationAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Long attemptId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "content_id")
    private LearningContent learningContent;

    @Column(name = "recognized_text") // STT 인식 결과
    private String recognizedText;

    @Column(name = "voice_score") // 발음 점수
    private int voiceScore;

    @Column(name = "lip_score") // 입모양 점수
    private int lipScore;

    @Column(name = "accuracy") // 정확도 (음성 70%, 입모양 30%)
    private double accuracy;

    @Column(name = "is_passed") // 합격 여부 (85% 이상 합격)
    private boolean isPassed;

    @Column(name = "user_pitch_data", columnDefinition = "json") // 사용자의 피치 데이터 (JSON 형식)
    private String userPitchData;
}
