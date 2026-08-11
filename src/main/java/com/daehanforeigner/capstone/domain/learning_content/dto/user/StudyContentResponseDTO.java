package com.daehanforeigner.capstone.domain.learning_content.dto.user;

import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.standard_pronunciation.entity.StandardPronunciation;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "발음 연습 화면용 학습 콘텐츠 상세")
public record StudyContentResponseDTO(
        @Schema(description = "학습 콘텐츠 ID", example = "10")
        Long contentId,

        @Schema(description = "카테고리 이름", example = "단어 학습")
        String categoryName,

        @Schema(description = "콘텐츠 유형", example = "WORD",
                allowableValues = {"SYLLABLE", "WORD", "SENTENCE"})
        ContentType contentType,

        @Schema(description = "난이도", example = "BEGINNER",
                allowableValues = {"BEGINNER", "INTERMEDIATE", "ADVANCED"})
        Difficulty difficulty,

        @Schema(description = "학습할 단어·문장 (화면 중앙 큰 글씨)", example = "사과")
        String text,

        @Schema(description = "표준 발음 표기", example = "[사과]")
        String standardPronunciationText,

        @Schema(description = "모국어 발음 표기", example = "sa-gwa")
        String nativePronunciation,

        @Schema(description = "의미", example = "apple")
        String meaning,

        @Schema(description = "예문", example = "사과를 먹었다.")
        String exampleSentence,

        @Schema(description = "발음 가이드 (입모양·혀 위치 설명)", example = "입을 크게 벌리고 '사'를 발음하세요.")
        String pronunciationGuide,

        @Schema(description = "원어민 음성 URL — 자료 미등록 시 null",
                example = "http://localhost:8080/images/audio/uuid.m4a")
        String answerAudioUrl,

        @Schema(description = "원어민 영상 URL — 자료 미등록 시 null",
                example = "http://localhost:8080/images/video/uuid.mp4")
        String answerVideoUrl
) {
    public static StudyContentResponseDTO from(LearningContent content, StandardPronunciation standardPronunciation) {
        return new StudyContentResponseDTO(
                content.getContentId(),
                content.getContentCategory() != null ? content.getContentCategory().getName() : null,
                content.getContentType(),
                content.getDifficulty(),
                content.getText(),
                content.getStandardPronunciationText(), // 표기는 LearningContent 소속
                content.getNativePronunciation(),
                content.getMeaning(),
                content.getExampleSentence(),
                content.getPronunciationGuide(),
                // 발음 자료가 아직 등록되지 않은 콘텐츠는 null일 수 있다
                standardPronunciation != null ? standardPronunciation.getAnswerAudioUrl() : null,
                standardPronunciation != null ? standardPronunciation.getAnswerVideoUrl() : null
        );
    }
}
