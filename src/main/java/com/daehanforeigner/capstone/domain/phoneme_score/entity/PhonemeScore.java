package com.daehanforeigner.capstone.domain.phoneme_score.entity;

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
@Table(name = "phoneme_score")
public class PhonemeScore extends GlobalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "phoneme_score_id")
    private Long phonemeScoreId;

    @ManyToOne
    @JoinColumn(name = "attempt_id")
    private PronunciationAttempt attempt;

    @Column(name = "phoneme") // 음소
    private String phoneme;

    @Column(name = "score") // 점수
    private int score;
}
