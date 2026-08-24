package com.daehanforeigner.capstone.domain.wrong_answer.service;

import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.wrong_answer.dto.WrongAnswerResponseDTO;
import com.daehanforeigner.capstone.domain.wrong_answer.dto.WrongAnswerSummaryResponseDTO;
import com.daehanforeigner.capstone.domain.wrong_answer.entity.WrongAnswer;
import com.daehanforeigner.capstone.domain.wrong_answer.entity.WrongAnswerTab;
import com.daehanforeigner.capstone.domain.wrong_answer.repository.WrongAnswerRepository;
import com.daehanforeigner.capstone.global.dto.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 오답 정리 담당.
// 오답 기록은 발음 평가에서 쌓이고 재시도해서 통과하면 해결 처리되므로, 여기서는 조회만 다룬다
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 조회 전용
public class WrongAnswerService {

    private final WrongAnswerRepository wrongAnswerRepository;

    // 오답 노트 목록 조회
    public PageResponseDTO<WrongAnswerResponseDTO> getWrongAnswers(
            Long userId, WrongAnswerTab tab, Boolean solved,
            Long categoryId, Difficulty difficulty, String keyword, Pageable pageable) {

        Page<WrongAnswer> page = wrongAnswerRepository.search(
                userId,
                tab != null ? tab.getContentTypes() : WrongAnswerTab.ALL.getContentTypes(),
                solved,
                categoryId,
                difficulty,
                normalizeKeyword(keyword),
                pageable);

        return PageResponseDTO.from(page.map(WrongAnswerResponseDTO::from));
    }

    // 오답 요약 + 정확도 분포
    public WrongAnswerSummaryResponseDTO getSummary(Long userId) {
        return wrongAnswerRepository.getSummary(userId, ContentType.SENTENCE);
    }

    // 빈 문자열·공백 검색어를 null로 정규화 (프론트가 ""를 보내도 전체 조회되도록)
    private String normalizeKeyword(String keyword) {
        return (keyword == null || keyword.isBlank()) ? null : keyword.trim();
    }
}
