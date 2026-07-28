package com.daehanforeigner.capstone.domain.learning_content.service;

import com.daehanforeigner.capstone.domain.learning_content.dto.LearningContentResponseDTO;
import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.repository.LearningContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningService {

    private final LearningContentRepository learningContentRepository;

    // 목록 조회
    public List<LearningContentResponseDTO> getContents(ContentType contentType, Difficulty difficulty) {

    }
}
