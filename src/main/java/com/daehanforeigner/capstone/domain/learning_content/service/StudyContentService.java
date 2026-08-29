package com.daehanforeigner.capstone.domain.learning_content.service;

import com.daehanforeigner.capstone.domain.content_category.entity.CategoryType;
import com.daehanforeigner.capstone.domain.learning_content.dto.user.StudyContentListResponseDTO;
import com.daehanforeigner.capstone.domain.learning_content.dto.user.StudyContentResponseDTO;
import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContentTranslation;
import com.daehanforeigner.capstone.domain.learning_content.entity.StudyStatus;
import com.daehanforeigner.capstone.domain.learning_content.repository.LearningContentRepository;
import com.daehanforeigner.capstone.domain.learning_content.repository.LearningContentTranslationRepository;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.repository.PronunciationAttemptRepository;
import com.daehanforeigner.capstone.domain.standard_pronunciation.entity.StandardPronunciation;
import com.daehanforeigner.capstone.domain.standard_pronunciation.repository.StandardPronunciationRepository;
import com.daehanforeigner.capstone.domain.user.entity.NativeLanguage;
import com.daehanforeigner.capstone.domain.user.entity.User;
import com.daehanforeigner.capstone.domain.user.repository.UserRepository;
import com.daehanforeigner.capstone.domain.wrong_answer.repository.WrongAnswerRepository;
import com.daehanforeigner.capstone.global.dto.PageResponseDTO;
import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import com.daehanforeigner.capstone.global.util.SortValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 회원용 학습 콘텐츠 조회 담당
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 조회 전용
public class StudyContentService {

    // 정렬을 허용할 필드. 등록 순서·난이도·가나다순 정도면 학습 목록에 충분하다
    private static final Set<String> SORTABLE_PROPERTIES =
            Set.of("contentId", "difficulty", "text");

    // 회원 모국어 번역이 없을 때 대신 보여줄 언어
    private static final NativeLanguage FALLBACK_LANGUAGE = NativeLanguage.EN;

    private final LearningContentRepository learningContentRepository;

    private final LearningContentTranslationRepository translationRepository;

    private final StandardPronunciationRepository standardPronunciationRepository;

    private final PronunciationAttemptRepository pronunciationAttemptRepository;

    private final WrongAnswerRepository wrongAnswerRepository;

    private final UserRepository userRepository;

    // 공부용·문화 학습 목록 조회 (카드 목록 화면)
    public PageResponseDTO<StudyContentListResponseDTO> getStudyContents(
            Long userId, CategoryType categoryType, Long categoryId,
            ContentType contentType, Difficulty difficulty,
            StudyStatus status, Pageable pageable) {

        SortValidator.validate(pageable, SORTABLE_PROPERTIES);

        // 1. 필터 조건으로 콘텐츠 페이지 조회.
        //    학습 상태 필터는 쿼리에서 처리해야 페이징 개수가 정확해진다
        Page<LearningContent> page = learningContentRepository.searchForUser(
                userId, categoryType, categoryId, contentType, difficulty,
                status != null ? status.name() : null, pageable);

        List<LearningContent> contents = page.getContent();

        // 조회 결과가 없으면 아래 배치 조회를 할 이유가 없다
        if (contents.isEmpty()) {
            return new PageResponseDTO<>(List.of(), page.getNumber(), page.getSize(),
                    page.getTotalElements(), page.getTotalPages(), page.hasNext());
        }

        // 2. 현재 페이지 콘텐츠에 대한 학습 기록·음성 URL을 한 번에 조회한다.
        //    콘텐츠마다 조회하면 N + 1이 되므로 IN 절로 묶는다.
        //    List 대신 Set으로 바꾸는 이유는 아래에서 contains를 콘텐츠 수만큼 호출하기 때문
        Set<Long> passedIds = new HashSet<>(
                pronunciationAttemptRepository.findPassedContentIds(userId, contents));
        Set<Long> unsolvedIds = new HashSet<>(
                wrongAnswerRepository.findUnsolvedContentIds(userId, contents));

        Map<Long, String> audioUrlMap = new HashMap<>();
        for (StandardPronunciation pronunciation :
                standardPronunciationRepository.findAllByLearningContentIn(contents)) {
            audioUrlMap.put(pronunciation.getLearningContent().getContentId(),
                    pronunciation.getAnswerAudioUrl());
        }

        // 3. 회원 모국어 번역을 일괄 조회 (대체 언어까지 함께 가져와 한 번에 끝낸다)
        NativeLanguage language = resolveLanguage(userId);
        Map<Long, LearningContentTranslation> translationMap = findTranslations(contents, language);

        // 4. 콘텐츠별 상태를 판정해 DTO로 변환
        Page<StudyContentListResponseDTO> dtoPage = page.map(content ->
                StudyContentListResponseDTO.from(
                        content,
                        translationMap.get(content.getContentId()),
                        audioUrlMap.get(content.getContentId()),
                        resolveStatus(content.getContentId(), passedIds, unsolvedIds)));

        return PageResponseDTO.from(dtoPage);
    }

    // 발음 연습 화면 진입 시 필요한 콘텐츠 상세 조회
    public StudyContentResponseDTO getStudyContent(Long userId, Long contentId) {
        LearningContent content = learningContentRepository.findById(contentId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_FOUND));

        // 발음 자료는 아직 등록되지 않았을 수 있으므로 예외 대신 null로 넘긴다 (DTO에서 방어)
        StandardPronunciation standardPronunciation = standardPronunciationRepository
                .findByLearningContent(content)
                .orElse(null);

        LearningContentTranslation translation = findTranslations(
                List.of(content), resolveLanguage(userId)).get(contentId);

        return StudyContentResponseDTO.from(content, translation, standardPronunciation);
    }

    // 회원의 모국어. 모국어를 지정하지 않은 회원은 대체 언어로 본다
    private NativeLanguage resolveLanguage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return user.getNativeLanguage() != null ? user.getNativeLanguage() : FALLBACK_LANGUAGE;
    }

    // 콘텐츠별로 보여줄 번역을 고른다.
    // 모국어 번역이 없으면 영어로 대체하고, 그마저 없으면 담지 않는다 (DTO에서 null 처리)
    private Map<Long, LearningContentTranslation> findTranslations(
            List<LearningContent> contents, NativeLanguage language) {

        Map<Long, LearningContentTranslation> result = new HashMap<>();

        // EnumSet을 쓰는 이유: 모국어가 영어면 Set.of(EN, EN)이 되어 중복 원소 예외가 난다
        for (LearningContentTranslation translation :
                translationRepository.findAllByLearningContentInAndLanguageIn(
                        contents, EnumSet.of(language, FALLBACK_LANGUAGE))) {

            Long contentId = translation.getLearningContent().getContentId();
            LearningContentTranslation selected = result.get(contentId);

            // 모국어 번역이 대체 언어보다 우선한다. 조회 순서를 보장할 수 없어 매번 비교한다
            if (selected == null || translation.getLanguage() == language) {
                result.put(contentId, translation);
            }
        }

        return result;
    }

    // 카드 배지에 표시할 학습 상태를 정한다.
    // 한 콘텐츠에 통과 기록과 오답 기록이 함께 있을 수 있어(틀렸다가 나중에 맞힌 경우) 판정 순서가 중요하다.
    // 통과 이력이 있으면 완료, 없으면 아직 통과하지 못한 시도로 보고 오답, 시도 자체가 없으면 미학습
    // 오답 여부는 WrongAnswer만 기준으로 삼는다.
    // 시도 기록까지 함께 보면 사용자가 오답 정리에서 삭제해도 배지가 계속 남는다
    private StudyStatus resolveStatus(Long contentId, Set<Long> passedIds, Set<Long> unsolvedIds) {
        if (passedIds.contains(contentId)) {
            return StudyStatus.COMPLETED;
        }
        if (unsolvedIds.contains(contentId)) {
            return StudyStatus.WRONG;
        }
        return StudyStatus.NOT_STARTED;
    }
}
