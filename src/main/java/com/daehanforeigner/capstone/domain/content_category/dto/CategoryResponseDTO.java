package com.daehanforeigner.capstone.domain.content_category.dto;

import com.daehanforeigner.capstone.domain.content_category.entity.CategoryType;
import com.daehanforeigner.capstone.domain.content_category.entity.ContentCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "학습 카테고리")
public record CategoryResponseDTO(
        @Schema(description = "카테고리 ID — 콘텐츠 등록 시 이 값을 categoryId로 보냅니다", example = "1")
        Long categoryId,

        @Schema(description = "카테고리 대분류", example = "BASIC", allowableValues = {"BASIC", "CULTURE"})
        CategoryType categoryType,

        @Schema(description = "카테고리 이름", example = "단어 학습")
        String name,

        @Schema(description = "카테고리 설명", example = "일상에서 자주 쓰는 단어를 학습합니다.")
        String description
) {
    public static CategoryResponseDTO from(ContentCategory contentCategory) {
        return new CategoryResponseDTO(
                contentCategory.getCategoryId(),
                contentCategory.getType(),
                contentCategory.getName(),
                contentCategory.getDescription()
        );
    }
}
