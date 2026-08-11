package com.daehanforeigner.capstone.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

// 페이징 응답 공통 포맷
// 스프링 Page를 그대로 응답하면 내부 필드(pageable, sort 등)가 노출되고
// 버전에 따라 JSON 구조가 바뀔 수 있어 필요한 값만 담아 감싼다
@Schema(description = "페이징 응답 공통 포맷")
public record PageResponseDTO<T>(
        @Schema(description = "현재 페이지 데이터")
        List<T> content,       // 현재 페이지 데이터

        @Schema(description = "현재 페이지 번호 — 0부터 시작합니다", example = "0")
        int page,              // 현재 페이지 번호 (0부터)

        @Schema(description = "페이지 크기", example = "20")
        int size,              // 페이지 크기

        @Schema(description = "필터 조건에 맞는 전체 개수", example = "137")
        long totalElements,    // 전체 개수

        @Schema(description = "전체 페이지 수", example = "7")
        int totalPages,        // 전체 페이지 수

        @Schema(description = "다음 페이지 존재 여부 — 무한 스크롤 시 이 값으로 판단하세요", example = "true")
        boolean hasNext        // 다음 페이지 존재 여부
) {
    public static <T> PageResponseDTO<T> from(Page<T> page) {
        return new PageResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
