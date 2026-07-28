package com.daehanforeigner.capstone.domain.admin.service;

import com.daehanforeigner.capstone.domain.learning_content.dto.LearningContentRequestDTO;
import com.daehanforeigner.capstone.domain.learning_content.dto.LearningContentResponseDTO;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.learning_content.repository.LearningContentRepository;
import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLearningContentService {

    private final LearningContentRepository learningContentRepository;

    // 전체 목록 조회
    public List<LearningContentResponseDTO> getContents() {
        return learningContentRepository.findAll().stream()
                .map(LearningContentResponseDTO::from)
                .toList();
    }

    // 등록
    @Transactional
    public Long createContent(LearningContentRequestDTO request) {
        return learningContentRepository.save(request.toEntity()).getContentId();
    }

    // 수정
    @Transactional
    public void updateContent(Long contentId, LearningContentRequestDTO request) {
        findContent(contentId).update(request.contentType(), request.difficulty(), request.text(),
                request.meaning(), request.exampleSentence(), request.pronunciationGuide());
    }

    // 삭제
    @Transactional
    public void deleteContent(Long contentId) {
        learningContentRepository.delete(findContent(contentId));
    }

    // 수정·삭제 시 대상 조회 (없으면 404)
    private LearningContent findContent(Long contentId) {
        return learningContentRepository.findById(contentId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_FOUND));
    }
}