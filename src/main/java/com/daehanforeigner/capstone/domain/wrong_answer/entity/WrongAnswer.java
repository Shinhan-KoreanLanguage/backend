package com.daehanforeigner.capstone.domain.wrong_answer.entity;

import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.user.entity.User;
import com.daehanforeigner.capstone.global.entity.GlobalEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
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
}

