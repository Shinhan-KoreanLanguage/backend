package com.daehanforeigner.capstone.domain.game_result.dto;

import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

// 게임 시작 응답 — 제한 시간 동안 제시할 단어 목록을 한 번에 내려준다
@Schema(description = "발음 게임 시작 결과")
public record GameStartResponseDTO(
        @Schema(description = "게임 세션 ID — 단어 제출·게임 종료 API에서 사용", example = "7")
        Long gameResultId,

        @Schema(description = "제시할 단어 목록 (셔플됨) — 프론트에서 순서대로 하나씩 보여주면 됩니다")
        List<WordItem> words) {

    @Schema(description = "게임에서 제시되는 단어 하나")
    public record WordItem(
            @Schema(description = "단어(콘텐츠) ID — 단어 제출 시 그대로 보내야 합니다", example = "10")
            Long wordId,

            @Schema(description = "단어 원문", example = "사과")
            String text) {
    }

    public static GameStartResponseDTO of(Long gameResultId, List<LearningContent> words) {
        return new GameStartResponseDTO(
                gameResultId,
                words.stream()
                        .map(w -> new WordItem(w.getContentId(), w.getText()))
                        .toList());
    }
}
