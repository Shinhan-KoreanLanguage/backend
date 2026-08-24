package com.daehanforeigner.capstone.domain.game_result.repository;

import com.daehanforeigner.capstone.domain.game_result.entity.GameResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameResultRepository extends JpaRepository<GameResult, Long> {
}
