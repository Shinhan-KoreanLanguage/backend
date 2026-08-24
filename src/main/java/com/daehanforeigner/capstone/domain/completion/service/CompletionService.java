package com.daehanforeigner.capstone.domain.completion.service;

import com.daehanforeigner.capstone.domain.completion.dto.CompletionResponseDTO;
import com.daehanforeigner.capstone.domain.completion.dto.CompletionResponseDTO.CategoryCompletion;
import com.daehanforeigner.capstone.domain.learning_content.repository.LearningContentRepository;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.repository.PronunciationAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 학습 완료율 담당 — 전체 콘텐츠 중 합격(서로 다른 콘텐츠 기준)한 비율
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompletionService {

    private final LearningContentRepository learningContentRepository;

    private final PronunciationAttemptRepository pronunciationAttemptRepository;

    public CompletionResponseDTO getCompletion(Long userId) {
        // 카테고리별 전체 콘텐츠 수 (분모) — [categoryId, categoryName, count]
        List<Object[]> totalRows = learningContentRepository.countGroupByCategory();

        // 카테고리별 합격한 콘텐츠 수 (분자) — [categoryId, count]
        Map<Long, Long> passedByCategory = pronunciationAttemptRepository
                .countDistinctPassedContentsGroupByCategory(userId).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

        List<CategoryCompletion> categories = totalRows.stream()
                .map(row -> {
                    Long categoryId = (Long) row[0];
                    String categoryName = (String) row[1];
                    long totalCount = (Long) row[2];
                    long passedCount = passedByCategory.getOrDefault(categoryId, 0L);

                    return new CategoryCompletion(
                            categoryId, categoryName, rate(passedCount, totalCount), totalCount, passedCount);
                })
                .toList();

        long totalContentCount = categories.stream().mapToLong(CategoryCompletion::totalCount).sum();
        long passedContentCount = categories.stream().mapToLong(CategoryCompletion::passedCount).sum();

        return new CompletionResponseDTO(
                rate(passedContentCount, totalContentCount),
                totalContentCount,
                passedContentCount,
                categories
        );
    }

    // 콘텐츠가 하나도 없는 카테고리(분모 0)는 0%로 처리해 나눗셈 예외를 피한다
    private double rate(long passedCount, long totalCount) {
        if (totalCount == 0) {
            return 0.0;
        }
        return Math.round(passedCount * 1000.0 / totalCount) / 10.0;
    }
}
