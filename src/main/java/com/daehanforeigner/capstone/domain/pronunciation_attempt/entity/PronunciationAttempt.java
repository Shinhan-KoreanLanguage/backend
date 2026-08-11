package com.daehanforeigner.capstone.domain.pronunciation_attempt.entity;

import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    // AI가 소수점 점수를 주고, 웹캠을 거부하면 입모양 점수가 null로 오므로 원시 타입(int)을 쓸 수 없다
    @Column(name = "voice_score") // 발음 점수 (stt_accuracy)
    private Double voiceScore;

    @Column(name = "lip_score") // 입모양 점수 (mouth_accuracy) — 영상 미전송 시 null
    private Double lipScore;

    @Column(name = "pitch_score") // 억양 점수 (pitch_accuracy) — 종합 점수에는 안 들어가는 참고 지표
    private Double pitchScore;

    @Column(name = "accuracy") // 종합 정확도 (음성 70%, 입모양 30%)
    private double accuracy;

    @Column(name = "is_passed") // 합격 여부
    private boolean isPassed;

    @Column(name = "user_pitch_data", columnDefinition = "json") // 사용자의 피치 곡선
    private String userPitchData;

    @Column(name = "pitch_highlight_segments", columnDefinition = "json") // 억양이 어긋난 구간 (화면 강조용)
    private String pitchHighlightSegments;

    @Column(name = "length_mismatch") // 발음 길이가 원어민과 크게 다른지
    private Boolean lengthMismatch;
}
