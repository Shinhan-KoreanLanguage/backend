package com.daehanforeigner.capstone.domain.standard_pronunciation.repository;

import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.standard_pronunciation.entity.StandardPronunciation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StandardPronunciationRepository extends JpaRepository<StandardPronunciation, Long> {

    // 콘텐츠 수정 시 기존 발음 자료 찾아 파일만 교체하기 위해 사용
    Optional<StandardPronunciation> findByLearningContent(LearningContent learningContent);

    // 콘텐츠 단건 삭제 시 연관 발음 자료 제거
    void deleteByLearningContent(LearningContent learningContent);

    // 콘텐츠 일괄 삭제 시 연관 발음 자료 제거
    void deleteAllByLearningContentIn(List<LearningContent> learningContents);

    // 목록 조회 시 현재 페이지 콘텐츠 발음 자료 한 번에 조회 (N + 1 문제 해결)
    List<StandardPronunciation> findAllByLearningContentIn(List<LearningContent> learningContents);
}