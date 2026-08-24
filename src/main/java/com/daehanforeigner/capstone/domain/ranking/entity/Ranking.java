package com.daehanforeigner.capstone.domain.ranking.entity;

import com.daehanforeigner.capstone.domain.user.entity.User;
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
@Table(name = "ranking")
public class Ranking extends GlobalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rank_id")
    private Long rankId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "best_score") // 역대 한 게임 최고 단어 개수
    private int bestScore;

    @Column(name = "spoken_word_count") // 지금까지 모든 게임에서 말한 누적 단어 수
    private int spokenWordCount;

    // 게임 종료 시 호출 — 최고 기록은 갱신될 때만 갱신, 누적 단어 수는 매번 더한다.
    // 반환값은 이번 판이 신기록인지 여부 (프론트 알림용)
    public boolean recordGame(int score) {
        this.spokenWordCount += score;

        boolean isNewBest = score > this.bestScore;
        if (isNewBest) {
            this.bestScore = score;
        }
        return isNewBest;
    }
}
