package com.daehanforeigner.capstone.domain.ranking.entity;

import com.daehanforeigner.capstone.domain.user.entity.User;
import com.daehanforeigner.capstone.global.entity.GlobalEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ranking")
public class Ranking extends GlobalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rank_id")
    private Long rankId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "best_score")
    private int bestScore;

    @Column(name = "spoken_word_count") // 말한 단어 수
    private int spokenWordCount;
}
