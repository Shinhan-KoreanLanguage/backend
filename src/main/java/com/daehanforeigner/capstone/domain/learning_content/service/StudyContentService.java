package com.daehanforeigner.capstone.domain.learning_content.service;

import com.daehanforeigner.capstone.domain.learning_content.dto.user.StudyContentResponseDTO;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.learning_content.repository.LearningContentRepository;
import com.daehanforeigner.capstone.domain.standard_pronunciation.entity.StandardPronunciation;
import com.daehanforeigner.capstone.domain.standard_pronunciation.repository.StandardPronunciationRepository;
import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 회원용 학습 콘텐츠 조회 담당
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 조회 전용
public class StudyContentService {

    private final LearningContentRepository learningContentRepository;

    private final StandardPronunciationRepository standardPronunciationRepository;

    // 발음 연습 화면 진입 시 필요한 콘텐츠 상세 조회
    public StudyContentResponseDTO getStudyContent(Long contentId) {
        LearningContent content = learningContentRepository.findById(contentId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_FOUND));

        // 발음 자료는 아직 등록되지 않았을 수 있으므로 예외 대신 null로 넘긴다 (DTO에서 방어)
        StandardPronunciation standardPronunciation = standardPronunciationRepository
                .findByLearningContent(content)
                .orElse(null);

        return StudyContentResponseDTO.from(content, standardPronunciation);
    }
}