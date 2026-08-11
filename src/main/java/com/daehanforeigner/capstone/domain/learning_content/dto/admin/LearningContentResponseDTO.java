package com.daehanforeigner.capstone.domain.learning_content.dto.admin;

import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.standard_pronunciation.entity.StandardPronunciation;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 학습 콘텐츠 목록 항목")
public record LearningContentResponseDTO(
        @Schema(description = "학습 콘텐츠 ID", example = "10")
        Long learningContentId, // 학습 컨텐츠 ID

        @Schema(description = "카테고리 ID", example = "1")
        Long categoryId, // 카테고리 ID

        @Schema(description = "카테고리 이름", example = "단어 학습")
        String categoryName, // 카테고리 이름

        @Schema(description = "콘텐츠 유형", example = "WORD",
                allowableValues = {"SYLLABLE", "WORD", "SENTENCE"})
        ContentType contentType, // 컨텐츠 유형 (음절, 단어, 문장)

        @Schema(description = "난이도", example = "BEGINNER",
                allowableValues = {"BEGINNER", "INTERMEDIATE", "ADVANCED"})
        Difficulty difficulty, // 난이도

        @Schema(description = "학습할 단어·문장", example = "사과")
        String text, // 학습 단어 or 문장

        @Schema(description = "의미", example = "apple")
        String meaning, // 의미

        @Schema(description = "예문", example = "사과를 먹었다.")
        String exampleSentence, // 예문

        @Schema(description = "발음 가이드", example = "입을 크게 벌리고 '사'를 발음하세요.")
        String pronunciationGuide, // 발음 가이드

        @Schema(description = "표준 발음 표기", example = "[사과]")
        String standardPronunciationText, // 표준 발음 표기

        @Schema(description = "모국어 발음 표기", example = "sa-gwa")
        String nativePronunciation, // 모국어 발음 표기

        @Schema(description = "원어민 음성 URL — 자료 미등록 시 null",
                example = "http://localhost:8080/images/audio/uuid.m4a")
        String answerAudioUrl, // 정답 오디오 URL

        @Schema(description = "원어민 영상 URL — 자료 미등록 시 null",
                example = "http://localhost:8080/images/video/uuid.mp4")
        String answerVideoUrl // 정답 비디오 URL

) {
    public static LearningContentResponseDTO from(LearningContent learningContent, StandardPronunciation pronunciation) {
        return new LearningContentResponseDTO(
                learningContent.getContentId(),
                learningContent.getContentCategory() != null ? learningContent.getContentCategory().getCategoryId() : null,
                learningContent.getContentCategory() != null ? learningContent.getContentCategory().getName() : null,
                learningContent.getContentType(),
                learningContent.getDifficulty(),
                learningContent.getText(),
                learningContent.getMeaning(),
                learningContent.getExampleSentence(),
                learningContent.getPronunciationGuide(),
                learningContent.getStandardPronunciationText(),
                learningContent.getNativePronunciation(),
                pronunciation != null ? pronunciation.getAnswerAudioUrl() : null,
                pronunciation != null ? pronunciation.getAnswerVideoUrl() : null
        );
    }
}
