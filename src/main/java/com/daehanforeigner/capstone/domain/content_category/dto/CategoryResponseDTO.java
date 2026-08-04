package com.daehanforeigner.capstone.domain.content_category.dto;

import com.daehanforeigner.capstone.domain.content_category.entity.CategoryType;
import com.daehanforeigner.capstone.domain.content_category.entity.ContentCategory;

public record CategoryResponseDTO(
        Long categoryId,
        CategoryType categoryType,
        String name,
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
