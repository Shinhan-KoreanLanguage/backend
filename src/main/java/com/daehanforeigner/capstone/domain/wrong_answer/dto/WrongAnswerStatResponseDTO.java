package com.daehanforeigner.capstone.domain.wrong_answer.dto;

import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.wrong_answer.entity.WrongAnswer;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "오답(틀린 단어·문장) 통계")
public record WrongAnswerStatResponseDTO(
        @Schema(description = "콘텐츠 ID", example = "12")
        Long contentId,

        @Schema(description = "학습 텍스트", example = "안녕하세요")
        String text,

        @Schema(description = "콘텐츠 유형") ContentType contentType,

        @Schema(description = "난이도") Difficulty difficulty,

        @Schema(description = "틀린 횟수", example = "5")
        int wrongCount,

        @Schema(description = "마지막으로 시도한 시각")
        LocalDateTime lastAttemptedAt
) {
    public static WrongAnswerStatResponseDTO from(WrongAnswer wrongAnswer) {
        LearningContent content = wrongAnswer.getLearningContent();

        return new WrongAnswerStatResponseDTO(
                content.getContentId(),
                content.getText(),
                content.getContentType(),
                content.getDifficulty(),
                wrongAnswer.getWrongCount(),
                wrongAnswer.getLastAttemptedAt()
        );
    }
}
