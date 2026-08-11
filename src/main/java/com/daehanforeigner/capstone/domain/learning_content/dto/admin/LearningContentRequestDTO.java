package com.daehanforeigner.capstone.domain.learning_content.dto.admin;

import com.daehanforeigner.capstone.domain.content_category.entity.ContentCategory;
import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "학습 콘텐츠 등록·수정 요청 (multipart의 content 파트에 JSON으로 담습니다)")
public record LearningContentRequestDTO(

        @Schema(description = "카테고리 ID — GET /admin/categories 응답에서 선택", example = "1")
        @NotNull(message = "카테고리 ID는 필수입니다.")
        Long categoryId,

        @Schema(description = "콘텐츠 유형", example = "WORD",
                allowableValues = {"SYLLABLE", "WORD", "SENTENCE"})
        @NotNull(message = "콘텐츠 유형 선택은 필수입니다.")
        ContentType contentType,

        @Schema(description = "난이도", example = "BEGINNER",
                allowableValues = {"BEGINNER", "INTERMEDIATE", "ADVANCED"})
        @NotNull(message = "난이도 선택은 필수입니다.")
        Difficulty difficulty,

        @Schema(description = "학습할 단어·문장", example = "사과")
        @NotBlank(message = "학습 텍스트는 필수입니다.")
        String text,

        @Schema(description = "의미", example = "apple")
        String meaning,

        @Schema(description = "예문", example = "사과를 먹었다.")
        String exampleSentence,

        @Schema(description = "발음 가이드 (입모양·혀 위치 설명)", example = "입을 크게 벌리고 '사'를 발음하세요.")
        String pronunciationGuide,

        @Schema(description = "표준 발음 표기", example = "[사과]")
        String standardPronunciationText, // 표준 발음 표기

        @Schema(description = "모국어 발음 표기", example = "sa-gwa")
        String nativePronunciation // 모국어 발음 표기
) {
        // DTO를 엔티티로 변환하는 메서드
        public LearningContent toEntity(ContentCategory contentCategory) {
            return LearningContent.builder()
                    .contentCategory(contentCategory)
                    .contentType(contentType)
                    .difficulty(difficulty)
                    .text(text)
                    .meaning(meaning)
                    .exampleSentence(exampleSentence)
                    .pronunciationGuide(pronunciationGuide)
                    .standardPronunciationText(standardPronunciationText)
                    .nativePronunciation(nativePronunciation)
                    .build();
        }
}
