package com.daehanforeigner.capstone.domain.game_field_table.entity;

import com.daehanforeigner.capstone.domain.game_result.entity.GameResult;
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
@Table(name = "game_field_table")
public class GameFieldTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_sentence_id") // 게임 문장 ID
    private Long gameSentenceId;

    @ManyToOne
    @JoinColumn(name = "game_result_id") // 게임 결과 ID
    private GameResult gameResult;

    @Column(name = "is_sentence") // 문장 여부 (단어 게임에서는 항상 false)
    private boolean isSentence;

    @Column(name = "game_field") // 내용 (제시된 단어 원문)
    private String gameField;

    @Column(name = "accuracy") // AI 분석 원점수 (참고용, null 가능)
    private Double accuracy;

    @Column(name = "is_correct") // accuracy가 판정 임계치 이상인지 — 점수 카운트 기준
    private boolean isCorrect;

    @Column(name = "sequence_no") // 몇 번째로 제시된 단어인지
    private int sequenceNo;
}
