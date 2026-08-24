package com.daehanforeigner.capstone.domain.phoneme_score.entity;

import com.daehanforeigner.capstone.domain.pronunciation_attempt.entity.PronunciationAttempt;
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
@Table(name = "phoneme_score")
public class PhonemeScore extends GlobalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "phoneme_score_id")
    private Long phonemeScoreId;

    @ManyToOne
    @JoinColumn(name = "attempt_id")
    private PronunciationAttempt attempt;

    @Column(name = "phoneme")
    private String phoneme;

    @Column(name = "score")
    private Double score;

    @Column(name = "is_weak") // 취약 음절 여부 (추천 연습 대상 선정용)
    private Boolean isWeak;

    @Column(name = "start_time") // 구간 시작 (초) — 파형 음절 분할에 사용
    private Double startTime;

    @Column(name = "end_time") // 구간 끝 (초)
    private Double endTime;
}