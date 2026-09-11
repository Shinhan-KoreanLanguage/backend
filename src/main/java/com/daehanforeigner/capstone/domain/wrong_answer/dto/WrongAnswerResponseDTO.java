package com.daehanforeigner.capstone.domain.wrong_answer.dto;

import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContentTranslation;
import com.daehanforeigner.capstone.domain.wrong_answer.entity.WrongAnswer;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

// 오답 정리 화면의 목록 한 줄.
// 다시 연습하려면 contentId로 발음 연습 화면에 진입하면 되므로 별도 재도전 API는 두지 않는다
@Schema(description = "오답 정리 목록 항목")
public record WrongAnswerResponseDTO(
        @Schema(description = "오답 기록 ID", example = "3")
        Long wrongId,

        @Schema(description = "학습 콘텐츠 ID — 다시 연습하러 이동할 때 사용합니다", example = "10")
        Long contentId,

        @Schema(description = "카테고리 이름", example = "단어 학습")
        String categoryName,

        @Schema(description = "콘텐츠 유형", example = "WORD",
                allowableValues = {"SYLLABLE", "WORD", "SENTENCE"})
        ContentType contentType,

        @Schema(description = "난이도", example = "BEGINNER",
                allowableValues = {"BEGINNER", "INTERMEDIATE", "ADVANCED"})
        Difficulty difficulty,

        @Schema(description = "학습할 단어·문장", example = "사과")
        String text,

        @Schema(description = "표준 발음 표기", example = "[사과]")
        String standardPronunciationText,

        @Schema(description = "회원 모국어로 된 뜻 — 한글 아래에 함께 표시하세요. 번역 미등록 시 null",
                example = "apple", nullable = true)
        String meaning,

        @Schema(description = "틀린 횟수", example = "8")
        int wrongCount,

        @Schema(description = "마지막 시도 정확도(%) — 아직 재시도 기록이 없으면 null", example = "58.0",
                nullable = true)
        Double lastAccuracy,

        @Schema(description = "해결 여부 — 재시도해 통과하면 true", example = "false")
        boolean solved,

        @Schema(description = "마지막으로 시도한 시각", example = "2026-08-18T16:16:13")
        LocalDateTime lastAttemptedAt
) {
    public static WrongAnswerResponseDTO from(WrongAnswer wrongAnswer, LearningContentTranslation translation) {
        LearningContent content = wrongAnswer.getLearningContent();

        return new WrongAnswerResponseDTO(
                wrongAnswer.getWrongId(),
                content.getContentId(),
                // 카테고리는 nullable이므로 방어
                content.getContentCategory() != null ? content.getContentCategory().getName() : null,
                content.getContentType(),
                content.getDifficulty(),
                content.getText(),
                content.getStandardPronunciationText(),
                // 번역이 등록되지 않은 콘텐츠일 수 있다
                translation != null ? translation.getMeaning() : null,
                wrongAnswer.getWrongCount(),
                wrongAnswer.getLastAccuracy(),
                wrongAnswer.isSolved(),
                wrongAnswer.getLastAttemptedAt()
        );
    }
}
