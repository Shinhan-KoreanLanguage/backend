package com.daehanforeigner.capstone.domain.feedback.entity;


import com.daehanforeigner.capstone.domain.pronunciation_attempt.entity.PronunciationAttempt;
import com.daehanforeigner.capstone.domain.user.entity.NativeLanguage;
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
@Table(name = "feedback")
public class Feedback extends GlobalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @ManyToOne
    @JoinColumn(name = "attempt_id")
    private PronunciationAttempt attempt;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type") // 피드백 항목
    private FeedbackType feedbackType;

    @Enumerated(EnumType.STRING)
    @Column(name = "level") // 피드백 평가 (좋아요, 보통, 취약)
    private FeedbackLevel level;

    @Column(name = "content") // 피드백 내용
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "language") // 피드백 언어
    private NativeLanguage language;
}