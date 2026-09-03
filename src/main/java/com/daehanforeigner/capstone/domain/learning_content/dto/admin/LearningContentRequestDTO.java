package com.daehanforeigner.capstone.domain.learning_content.dto.admin;

import com.daehanforeigner.capstone.domain.content_category.entity.ContentCategory;
import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "학습 콘텐츠 등록·수정 요청 (multipart의 content 파트에 JSON으로 담습니다)")
public record LearningContentRequestDTO(

        @Schema(description = "카테고리 ID — GET /admin/categories 응답에서 선택", example = "1")
        @NotNull(message = "카테고리 ID는 필수입니다.")
        Long categoryId,

        @Schema(description = "콘텐츠 유형", example = "WORD",
                allowableValues = {"SYLLABLE", "WORD", "SENTENCE"})
        @NotNull(message = "콘텐츠 유형 선택은 필수입니다.")
        ContentType contentType,

        @Schema(description = "난이도 — 난이도 구분이 없는 콘텐츠(실생활 문장 등)는 생략할 수 있습니다",
                example = "BEGINNER", allowableValues = {"BEGINNER", "INTERMEDIATE", "ADVANCED"},
                nullable = true)
        Difficulty difficulty,

        @Schema(description = "학습할 단어·문장 (한국어)", example = "사과")
        @NotBlank(message = "학습 텍스트는 필수입니다.")
        String text,

        @Schema(description = "예문", example = "사과를 먹었다.")
        String exampleSentence,

        @Schema(description = "표준 발음 표기 (한국어)", example = "[사과]")
        String standardPronunciationText,

        @Schema(description = """
                언어별 번역 목록. **KR · EN · JP · CN 네 언어를 모두 등록해야 합니다.**
                회원 모국어에 맞춰 화면을 채우는 구조라, 하나라도 빠지면 그 국적 회원은 빈 화면을 보게 됩니다.
                같은 언어를 두 번 넣으면 400 `DUPLICATE_TRANSLATION_LANGUAGE`,
                빠진 언어가 있으면 400 `MISSING_TRANSLATION_LANGUAGE`가 반환됩니다.
                """)
        @NotNull(message = "언어별 번역은 필수입니다.")
        @Valid
        List<TranslationRequestDTO> translations
) {
        // DTO를 엔티티로 변환하는 메서드.
        // standardPronunciationText는 KR 번역과 서로 보완되므로(둘 중 하나만 보내도 됨)
        // 서비스가 확정한 값을 받아서 넣는다.
        public LearningContent toEntity(ContentCategory contentCategory, String resolvedPronunciationText) {
            return LearningContent.builder()
                    .contentCategory(contentCategory)
                    .contentType(contentType)
                    .difficulty(difficulty)
                    .text(text)
                    .exampleSentence(exampleSentence)
                    .standardPronunciationText(resolvedPronunciationText)
                    .build();
        }
}
