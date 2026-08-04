package com.daehanforeigner.capstone.domain.admin.service;

import com.daehanforeigner.capstone.domain.content_category.entity.ContentCategory;
import com.daehanforeigner.capstone.domain.content_category.repository.ContentCategoryRepository;
import com.daehanforeigner.capstone.domain.learning_content.dto.LearningContentRequestDTO;
import com.daehanforeigner.capstone.domain.learning_content.dto.LearningContentResponseDTO;
import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.learning_content.repository.LearningContentRepository;
import com.daehanforeigner.capstone.domain.standard_pronunciation.entity.StandardPronunciation;
import com.daehanforeigner.capstone.domain.standard_pronunciation.repository.StandardPronunciationRepository;
import com.daehanforeigner.capstone.global.dto.PageResponseDTO;
import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import com.daehanforeigner.capstone.global.storage.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLearningContentService {

    private final LearningContentRepository learningContentRepository;

    private final ContentCategoryRepository contentCategoryRepository;

    private final StandardPronunciationRepository standardPronunciationRepository;

    private final FileService fileService;

    // 목록 조회 (유형·난이도·검색어 필터 + 페이징)
    public PageResponseDTO<LearningContentResponseDTO> getContents(Long categoryId, ContentType contentType, Difficulty difficulty,String keyword, Pageable pageable) {
        Page<LearningContentResponseDTO> page = learningContentRepository
                .searchContents(categoryId, contentType, difficulty, normalizeKeyword(keyword), pageable)
                .map(LearningContentResponseDTO::from); // 페이지 정보는 유지하고 내용만 DTO로 변환

        return PageResponseDTO.from(page);
    }

    // 등록
    @Transactional
    public Long createContent(LearningContentRequestDTO request, MultipartFile audioFile, MultipartFile videoFile) {
        ContentCategory category = findCategory(request.categoryId());
        LearningContent content = learningContentRepository.save(request.toEntity(category));

        String audioUrl = (audioFile != null && !audioFile.isEmpty())
                ? fileService.saveAudio(audioFile, "audio") : null;
        String videoUrl = (videoFile != null && !videoFile.isEmpty())
                ? fileService.saveVideo(videoFile, "video") : null;

        // 저장된 URL이 모두 null이 아닌 경우 생성함
        if (audioUrl != null && videoUrl != null) {
            standardPronunciationRepository.save(StandardPronunciation.builder()
                    .learningContent(content)
                    .answerAudioUrl(audioUrl)
                    .answerVideoUrl(videoUrl)
                    .build());
        }

        return content.getContentId();
    }

    // 수정
    @Transactional
    public void updateContent(Long contentId, LearningContentRequestDTO request, MultipartFile audioFile, MultipartFile videoFile) {
        ContentCategory category = findCategory(request.categoryId());
        LearningContent content = findContent(contentId);

        content.update(category, request.contentType(), request.difficulty(), request.text(),
                request.meaning(), request.exampleSentence(), request.pronunciationGuide());

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
        }
    }

    // 단일 삭제
    @Transactional
    public void deleteContent(Long contentId) {
        learningContentRepository.delete(findContent(contentId));
    }

    // 일괄 삭제
    @Transactional
    public void deleteContents(List<Long> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            throw new CustomException(ErrorCode.CONTENT_IDS_REQUIRED);
        }

        learningContentRepository.deleteAllById(contentIds);
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