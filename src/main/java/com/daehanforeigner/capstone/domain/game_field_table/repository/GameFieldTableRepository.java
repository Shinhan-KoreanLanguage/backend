package com.daehanforeigner.capstone.domain.game_field_table.repository;

import com.daehanforeigner.capstone.domain.game_field_table.entity.GameFieldTable;
import com.daehanforeigner.capstone.domain.game_result.entity.GameResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameFieldTableRepository extends JpaRepository<GameFieldTable, Long> {

    List<GameFieldTable> findAllByGameResultOrderBySequenceNoAsc(GameResult gameResult);

    int countByGameResultAndIsCorrectTrue(GameResult gameResult);

    int countByGameResult(GameResult gameResult); // 다음 단어의 sequenceNo 산정용
}
