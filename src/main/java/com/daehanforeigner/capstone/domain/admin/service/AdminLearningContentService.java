package com.daehanforeigner.capstone.domain.admin.service;

import com.daehanforeigner.capstone.domain.content_category.entity.ContentCategory;
import com.daehanforeigner.capstone.domain.content_category.repository.ContentCategoryRepository;
import com.daehanforeigner.capstone.domain.learning_content.dto.admin.LearningContentRequestDTO;
import com.daehanforeigner.capstone.domain.learning_content.dto.admin.LearningContentResponseDTO;
import com.daehanforeigner.capstone.domain.learning_content.dto.admin.TranslationRequestDTO;
import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContentTranslation;
import com.daehanforeigner.capstone.domain.learning_content.repository.LearningContentRepository;
import com.daehanforeigner.capstone.domain.learning_content.repository.LearningContentTranslationRepository;
import com.daehanforeigner.capstone.domain.standard_pronunciation.entity.StandardPronunciation;
import com.daehanforeigner.capstone.domain.standard_pronunciation.repository.StandardPronunciationRepository;
import com.daehanforeigner.capstone.domain.user.entity.NativeLanguage;
import com.daehanforeigner.capstone.global.ai.PronunciationAiClient;
import com.daehanforeigner.capstone.global.dto.PageResponseDTO;
import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import com.daehanforeigner.capstone.global.storage.FileService;
import com.daehanforeigner.capstone.global.util.SortValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLearningContentService {

    // 정렬을 허용할 필드. 관리자는 유형별 정렬도 쓰므로 회원용보다 하나 더 열어둔다
    private static final Set<String> SORTABLE_PROPERTIES =
            Set.of("contentId", "difficulty", "text", "contentType");

    private final LearningContentRepository learningContentRepository;

    private final LearningContentTranslationRepository translationRepository;

    private final ContentCategoryRepository contentCategoryRepository;

    private final StandardPronunciationRepository standardPronunciationRepository;

    private final FileService fileService;

    private final PronunciationAiClient aiClient;

    // 목록 조회 (유형·난이도·검색어 필터 + 페이징)
    public PageResponseDTO<LearningContentResponseDTO> getContents(Long categoryId, ContentType contentType, Difficulty difficulty, String keyword, Pageable pageable) {
        SortValidator.validate(pageable, SORTABLE_PROPERTIES);

        Page<LearningContent> page = learningContentRepository
                .searchContents(categoryId, contentType, difficulty, normalizeKeyword(keyword), pageable);

        // 현재 페이지 콘텐츠들의 발음 자료를 한 번에 조회 후 contentId로 매핑 (N+1 방지)
        Map<Long, StandardPronunciation> pronunciationMap = new HashMap<>();
        for (StandardPronunciation pronunciation :
                standardPronunciationRepository.findAllByLearningContentIn(page.getContent())) {
            pronunciationMap.put(pronunciation.getLearningContent().getContentId(), pronunciation);
        }

        // 번역도 같은 이유로 한 번에 조회한다. 관리자 화면은 등록된 언어 전체를 보여준다
        Map<Long, List<LearningContentTranslation>> translationMap = new HashMap<>();
        for (LearningContentTranslation translation :
                translationRepository.findAllByLearningContentInAndLanguageIn(
                        page.getContent(), List.of(NativeLanguage.values()))) {
            translationMap
                    .computeIfAbsent(translation.getLearningContent().getContentId(), key -> new ArrayList<>())
                    .add(translation);
        }

        Page<LearningContentResponseDTO> dtoPage = page.map(content ->
                LearningContentResponseDTO.from(
                        content,
                        pronunciationMap.get(content.getContentId()),
                        translationMap.getOrDefault(content.getContentId(), List.of())));

        return PageResponseDTO.from(dtoPage);
    }

    // 등록
    @Transactional
    public Long createContent(LearningContentRequestDTO request,
                              MultipartFile audioFile, MultipartFile videoFile) {
        ContentCategory category = findCategory(request.categoryId());

        // 등록 시에는 음성·영상이 모두 필요 (발음 연습 화면이 둘 다 사용)
        if (audioFile == null || audioFile.isEmpty() || videoFile == null || videoFile.isEmpty()) {
            throw new CustomException(ErrorCode.MEDIA_FILE_REQUIRED);
        }

        validateTranslations(request.translations());
        String pronunciationText = resolvePronunciationText(request);

        LearningContent content = learningContentRepository.save(request.toEntity(category, pronunciationText));

        saveTranslations(content, request.translations(), pronunciationText);

        // AI 서버 등록에 파일 URL이 필요하므로 저장 결과를 받아둔다
        StandardPronunciation pronunciation = standardPronunciationRepository.save(
                StandardPronunciation.builder()
                        .learningContent(content)
                        .answerAudioUrl(fileService.saveAudio(audioFile, "audio"))
                        .answerVideoUrl(fileService.saveVideo(videoFile, "video"))
                        .build());

        // AI 서버에 원어민 기준 등록 — 이게 있어야 사용자 발음 분석이 가능하다
        registerAiReference(content, pronunciation);

        return content.getContentId();
    }

    // 수정
    @Transactional
    public void updateContent(Long contentId, LearningContentRequestDTO request, MultipartFile audioFile, MultipartFile videoFile) {
        ContentCategory category = findCategory(request.categoryId());
        LearningContent content = findContent(contentId);

        validateTranslations(request.translations());
        String pronunciationText = resolvePronunciationText(request);

        content.update(category, request.contentType(), request.difficulty(),
                request.text(), request.exampleSentence(), pronunciationText);

        // 수정은 전체 교체 방식이므로 번역도 지우고 다시 넣는다.
        // flush를 하지 않으면 JPA가 INSERT를 DELETE보다 먼저 실행해 (content_id, language) 중복으로 실패한다
        translationRepository.deleteAllByLearningContent(content);
        translationRepository.flush();
        saveTranslations(content, request.translations(), pronunciationText);

        String audioUrl = (audioFile != null && !audioFile.isEmpty())
                ? fileService.saveAudio(audioFile, "audio") : null;
        String videoUrl = (videoFile != null && !videoFile.isEmpty())
                ? fileService.saveVideo(videoFile, "video") : null;

        if (audioUrl != null || videoUrl != null) {
            // 텍스트만 등록됐던 콘텐츠라면 발음 자료가 없으므로 새로 만든다
            StandardPronunciation pronunciation = standardPronunciationRepository
                    .findByLearningContent(content)
                    .orElseGet(() -> standardPronunciationRepository.save(
                            StandardPronunciation.builder().learningContent(content).build()));

            // 새로 올린 파일만 교체 (안 올린 쪽은 기존 URL 유지)
            if (audioUrl != null) {
                pronunciation.updateAudioUrl(audioUrl);
            }
            if (videoUrl != null) {
                pronunciation.updateVideoUrl(videoUrl);
            }

            // 파일이 바뀌었으니 AI 서버의 원어민 기준도 다시 등록한다
            registerAiReference(content, pronunciation);
        }
    }

    // 단일 삭제
    @Transactional
    public void deleteContent(Long contentId) {
        LearningContent content = findContent(contentId);

        standardPronunciationRepository.deleteByLearningContent(content);
        translationRepository.deleteAllByLearningContent(content);

        learningContentRepository.delete(content);
    }

    // 일괄 삭제
    @Transactional
    public void deleteContents(List<Long> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            throw new CustomException(ErrorCode.CONTENT_IDS_REQUIRED);
        }

        List<LearningContent> contents = learningContentRepository.findAllById(contentIds);

        // 연관된 발음 자료·번역 먼저 삭제
        standardPronunciationRepository.deleteAllByLearningContentIn(contents);
        translationRepository.deleteAllByLearningContentIn(contents);

        learningContentRepository.deleteAll(contents);
    }

    // 번역 목록 검증. 회원 모국어에 맞춰 화면을 채우는 구조라 네 언어가 모두 있어야
    // 특정 국적 회원만 빈 화면을 보는 일이 없다.
    private void validateTranslations(List<TranslationRequestDTO> translations) {
        Set<NativeLanguage> languages = new HashSet<>();

        for (TranslationRequestDTO translation : translations) {
            if (!languages.add(translation.language())) {
                throw new CustomException(ErrorCode.DUPLICATE_TRANSLATION_LANGUAGE);
            }

            // 모국어 발음 표기는 KR만 생략할 수 있다.
            // KR은 한국어 표준 발음 표기와 같은 정보이므로 서로 채워 넣을 수 있기 때문이다.
            if (translation.language() != NativeLanguage.KR && !StringUtils.hasText(translation.nativePronunciation())) {
                throw new CustomException(ErrorCode.NATIVE_PRONUNCIATION_REQUIRED);
            }
        }

        if (!languages.containsAll(EnumSet.allOf(NativeLanguage.class))) {
            throw new CustomException(ErrorCode.MISSING_TRANSLATION_LANGUAGE);
        }
    }

    // 한국어 표준 발음 표기를 확정한다.
    // 표준 발음 표기(예: [사꽈])와 KR 모국어 발음 표기는 같은 정보라, 관리자가 둘 중 하나만
    // 입력해도 되도록 서로 채워준다. 둘 다 비면 채울 근거가 없으므로 400으로 막는다.
    private String resolvePronunciationText(LearningContentRequestDTO request) {
        if (StringUtils.hasText(request.standardPronunciationText())) {
            return request.standardPronunciationText();
        }

        String fromKorean = request.translations().stream()
                .filter(translation -> translation.language() == NativeLanguage.KR)
                .map(TranslationRequestDTO::nativePronunciation)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.PRONUNCIATION_TEXT_REQUIRED));

        return fromKorean;
    }

    // 언어별 번역 저장. KR의 모국어 발음 표기가 비어 있으면 확정된 표준 발음 표기로 채운다
    private void saveTranslations(LearningContent content, List<TranslationRequestDTO> translations,
                                  String resolvedPronunciationText) {
        for (TranslationRequestDTO translation : translations) {
            String nativePronunciation = StringUtils.hasText(translation.nativePronunciation())
                    ? translation.nativePronunciation()
                    : resolvedPronunciationText;

            translationRepository.save(LearningContentTranslation.builder()
                    .learningContent(content)
                    .language(translation.language())
                    .meaning(translation.meaning())
                    .pronunciationGuide(translation.pronunciationGuide())
                    .nativePronunciation(nativePronunciation)
                    .build());
        }
    }

    // 원어민 영상·음성을 AI 서버에 등록하고, 추출된 피치 곡선을 받아 캐시한다.
    // 실패하면 예외가 전파되어 콘텐츠 등록도 롤백된다 — 기준 없는 콘텐츠가 생기는 것을 막기 위함
    private void registerAiReference(LearningContent content, StandardPronunciation pronunciation) {
        aiClient.registerReference(
                content.getText(),
                fileService.loadAsResource(pronunciation.getAnswerVideoUrl()),
                pronunciation.getAnswerAudioUrl() != null
                        ? fileService.loadAsResource(pronunciation.getAnswerAudioUrl()) : null);

        // 등록 응답에는 처리된 개수만 오므로, 곡선 좌표는 조회로 한 번 더 받아온다
        pronunciation.updateNativePitchData(
                aiClient.getReference(content.getText()).path("pitch_curve").toString());
    }

    // 카테고리 조회 (없으면 404)
    private ContentCategory findCategory(Long categoryId) {
        return contentCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    // 수정·삭제 시 대상 조회 (없으면 404)
    private LearningContent findContent(Long contentId) {
        return learningContentRepository.findById(contentId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_FOUND));
    }

    // 빈 문자열·공백 검색어를 null로 정규화 (프론트가 ""를 보내도 전체 조회되도록)
    private String normalizeKeyword(String keyword) {
        return (keyword == null || keyword.isBlank()) ? null : keyword.trim();
    }
}