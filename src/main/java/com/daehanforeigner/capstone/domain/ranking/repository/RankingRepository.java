package com.daehanforeigner.capstone.domain.ranking.repository;

import com.daehanforeigner.capstone.domain.ranking.entity.Ranking;
import com.daehanforeigner.capstone.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RankingRepository extends JpaRepository<Ranking, Long> {

    Optional<Ranking> findByUser(User user);

    // 동점 시 먼저 기록을 세운 사람이 앞서도록 rankId(등록 순) 기준으로 2차 정렬
    List<Ranking> findAllByOrderByBestScoreDescRankIdAsc(Pageable pageable);

    // 내 순위 계산용 — 나보다 점수가 높거나(같으면 더 먼저 기록한) 사람 수를 센다
    long countByBestScoreGreaterThan(int bestScore);
}
