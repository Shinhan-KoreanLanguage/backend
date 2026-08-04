package com.daehanforeigner.capstone.domain.learning_content.dto;

import com.daehanforeigner.capstone.domain.content_category.entity.ContentCategory;
import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LearningContentRequestDTO(

        @NotNull(message = "카테고리 ID는 필수입니다.")
        Long categoryId,

        @NotNull(message = "콘텐츠 유형 선택은 필수입니다.")
        ContentType contentType,

        @NotNull(message = "난이도 선택은 필수입니다.")
        Difficulty difficulty,

        @NotBlank(message = "학습 텍스트는 필수입니다.")
        String text,

        String meaning,

        String exampleSentence,

        String pronunciationGuide
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
                    .build();
        }
}
