package com.daehanforeigner.capstone.domain.learning_content.entity;

// 회원 기준 학습 상태
public enum StudyStatus {
    NOT_STARTED, // 시도 기록 없음
    WRONG,       // 오답 기록 있음 (isSolved = false)
    COMPLETED    // 통과 기록 있음 (isSolved = true)
}