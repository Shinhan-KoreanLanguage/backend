package com.daehanforeigner.capstone.domain.completion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "학습 완료율")
public record CompletionResponseDTO(
        @Schema(description = "전체 학습 완료율(%) — 합격한 콘텐츠 수 ÷ 전체 콘텐츠 수", example = "42.5")
        double overallRate,

        @Schema(description = "전체 콘텐츠 수", example = "200")
        long totalContentCount,

        @Schema(description = "합격한(서로 다른) 콘텐츠 수 — 같은 콘텐츠를 여러 번 합격해도 1로 카운트", example = "85")
        long passedContentCount,

        @Schema(description = "카테고리별 완료율")
        List<CategoryCompletion> categories
) {
    @Schema(description = "카테고리별 학습 완료율")
    public record CategoryCompletion(
            @Schema(description = "카테고리 ID", example = "3")
            Long categoryId,

            @Schema(description = "카테고리 이름", example = "K-POP 가사")
            String categoryName,

            @Schema(description = "해당 카테고리 완료율(%)", example = "60.0")
            double rate,

            @Schema(description = "해당 카테고리 전체 콘텐츠 수", example = "20")
            long totalCount,

            @Schema(description = "해당 카테고리 합격 콘텐츠 수", example = "12")
            long passedCount
    ) {
    }
}
