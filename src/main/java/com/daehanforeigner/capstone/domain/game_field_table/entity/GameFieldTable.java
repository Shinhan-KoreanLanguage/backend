package com.daehanforeigner.capstone.domain.game_field_table.entity;

import com.daehanforeigner.capstone.domain.game_result.entity.GameResult;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "game_field_table")
public class GameFieldTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_sentence_id") // 게임 문장 ID
    private Long gameSentenceId;

    @ManyToOne
    @JoinColumn(name = "game_result_id") // 게임 결과 ID
    private GameResult gameResult;

    @Column(name = "is_sentence") // 문장 여부
    private boolean isSentence;

    @Column(name = "game_field") // 내용
    private String gameField;
}
