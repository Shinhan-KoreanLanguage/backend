package com.daehanforeigner.capstone.domain.wrong_answer.entity;

import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.user.entity.User;
import com.daehanforeigner.capstone.global.entity.GlobalEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "wrong_answer")
public class WrongAnswer extends GlobalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wrong_id")
    private Long wrongId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "content_id")
    private LearningContent learningContent;

    @Column(name = "wrong_count") // 틀린 횟수
    private int wrongCount;

    @Column(name = "is_solved") // 해결 여부
    private boolean isSolved;

    @Column(name = "last_attempted_at") // 마지막 시도 날짜
    private LocalDateTime lastAttemptedAt;

    // 오답 노트 목록과 정확도 분포에 쓰는 값.
    // 평균이 아니라 최근 점수를 담는다 — 여러 번 틀린 뒤 한 번 잘해도 평균은 거의 안 움직여
    // 학습자가 나아지고 있다는 걸 화면에서 볼 수 없기 때문이다
    @Column(name = "last_accuracy") // 마지막 시도 정확도
    private Double lastAccuracy;

    // 재시도 결과 반영 — 틀리면 횟수를 올리고, 맞히면 해결 처리
    public void recordAttempt(boolean passed, double accuracy) {
        this.lastAttemptedAt = LocalDateTime.now();
        this.lastAccuracy = accuracy;

        if (passed) {
            this.isSolved = true;
        } else {
            this.wrongCount++;
            this.isSolved = false;
        }
    }
}