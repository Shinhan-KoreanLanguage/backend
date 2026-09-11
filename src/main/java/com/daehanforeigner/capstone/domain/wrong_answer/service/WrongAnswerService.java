package com.daehanforeigner.capstone.domain.wrong_answer.service;

import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContentTranslation;
import com.daehanforeigner.capstone.domain.learning_content.service.ContentTranslationResolver;
import com.daehanforeigner.capstone.domain.user.entity.NativeLanguage;
import com.daehanforeigner.capstone.domain.wrong_answer.dto.WrongAnswerResponseDTO;
import com.daehanforeigner.capstone.domain.wrong_answer.dto.WrongAnswerSummaryResponseDTO;
import com.daehanforeigner.capstone.domain.wrong_answer.entity.WrongAnswer;
import com.daehanforeigner.capstone.domain.wrong_answer.entity.WrongAnswerTab;
import com.daehanforeigner.capstone.domain.wrong_answer.repository.WrongAnswerRepository;
import com.daehanforeigner.capstone.global.dto.PageResponseDTO;
import com.daehanforeigner.capstone.global.util.SortValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

// 오답 정리 담당.
// 오답 기록은 발음 평가에서 쌓이고 재시도해서 통과하면 해결 처리되므로, 여기서는 조회만 다룬다
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 조회 전용
public class WrongAnswerService {

    // 정렬을 허용할 필드. 화면의 정렬 옵션(가장 많이 틀린 순·최근 학습 순·정확도 낮은 순)과 대응한다
    private static final Set<String> SORTABLE_PROPERTIES =
            Set.of("lastAttemptedAt", "wrongCount", "lastAccuracy");

    private final WrongAnswerRepository wrongAnswerRepository;

    private final ContentTranslationResolver translationResolver;

    // 오답 노트 목록 조회
    public PageResponseDTO<WrongAnswerResponseDTO> getWrongAnswers(
            Long userId, WrongAnswerTab tab, Boolean solved,
            Long categoryId, Difficulty difficulty, String keyword, Pageable pageable) {

        SortValidator.validate(pageable, SORTABLE_PROPERTIES);

        Page<WrongAnswer> page = wrongAnswerRepository.search(
                userId,
                tab != null ? tab.getContentTypes() : WrongAnswerTab.ALL.getContentTypes(),
                solved,
                categoryId,
                difficulty,
                normalizeKeyword(keyword),
                pageable);

        // 회원 모국어 번역을 일괄 조회 (한글 아래에 함께 표시할 뜻)
        List<LearningContent> contents = page.getContent().stream()
                .map(WrongAnswer::getLearningContent)
                .toList();
        NativeLanguage language = translationResolver.resolveLanguage(userId);
        Map<Long, LearningContentTranslation> translationMap = translationResolver.resolve(contents, language);

        return PageResponseDTO.from(page.map(wrongAnswer ->
                WrongAnswerResponseDTO.from(
                        wrongAnswer,
                        translationMap.get(wrongAnswer.getLearningContent().getContentId()))));
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
