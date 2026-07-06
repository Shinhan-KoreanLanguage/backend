package com.daehanforeigner.capstone.feedback.entity;

import com.daehanforeigner.capstone.domain.pronunciation_attempt.PromunciationAttempt;
import com.daehanforeigner.capstone.domain.user.entity.NativeLangauge;
import com.daehanforeigner.capstone.global.entity.GlobalEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "feedback")
public class FeedBack extends GlobalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private int feedbackId;

    @ManyToOne
    @JoinColumn(name = "attempt_id")
    private PromunciationAttempt attempt;

    @Column(name = "content") // 피드백 내용
    private String content;

    @Column(name = "language") // 피드백 언어
    private NativeLangauge language;
}
