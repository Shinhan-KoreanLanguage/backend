package com.daehanforeigner.capstone.domain.game_result.entity;

import com.daehanforeigner.capstone.domain.content_category.entity.ContentCategory;
import com.daehanforeigner.capstone.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "game_result")
public class GameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_result_id")
    private Long gameResultId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private ContentCategory category;

    @Column(name = "score") // 정확히 발음한 단어 개수
    private int score;

    @Column(name = "is_passed") // 시간을 다 채우고 정상 종료했는지 여부
    private boolean isPassed;

    @Column(name = "played_at")
    private LocalDateTime playedAt;

    // 게임 종료 시 최종 점수를 반영한다 (더티 체킹)
    public void finish(int score, boolean isPassed) {
        this.score = score;
        this.isPassed = isPassed;
    }
}
