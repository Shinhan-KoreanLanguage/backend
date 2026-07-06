package com.daehanforeigner.capstone.global.entity;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

public class GlobalEntityListener {

    @PrePersist
    public void prePersist(GlobalEntity entity) {
        // 생성일과 수정일을 현재 시간으로 설정해서 저장
        LocalDateTime now = LocalDateTime.now();

        entity.setCreatedAt(now);

        entity.setUpdatedAt(now);
    }

    @PreUpdate
    public void preUpdate(GlobalEntity entity) {
        // 수정일을 현재 시간으로 설정해서 저장
        entity.setUpdatedAt(LocalDateTime.now());
    }
}
