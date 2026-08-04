package com.daehanforeigner.capstone.domain.standard_pronunciation.repository;

import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.standard_pronunciation.entity.StandardPronunciation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StandardPronunciationRepository extends JpaRepository<StandardPronunciation, Long> {

    // 콘텐츠 수정 시 기존 발음 자료 찾아 파일만 교체하기 위해 사용
    Optional<StandardPronunciation> findByLearningContent(LearningContent learningContent);
}
