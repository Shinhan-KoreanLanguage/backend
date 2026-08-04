package com.daehanforeigner.capstone.global.dto;

import org.springframework.data.domain.Page;

import java.util.List;

// 페이징 응답 공통 포맷
// 스프링 Page를 그대로 응답하면 내부 필드(pageable, sort 등)가 노출되고
// 버전에 따라 JSON 구조가 바뀔 수 있어 필요한 값만 담아 감싼다
public record PageResponseDTO<T>(
        List<T> content,       // 현재 페이지 데이터
        int page,              // 현재 페이지 번호 (0부터)
        int size,              // 페이지 크기
        long totalElements,    // 전체 개수
        int totalPages,        // 전체 페이지 수
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