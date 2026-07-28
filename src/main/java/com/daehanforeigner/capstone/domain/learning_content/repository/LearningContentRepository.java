package com.daehanforeigner.capstone.domain.learning_content.repository;

import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningContentRepository extends JpaRepository<LearningContent, Long> {

    // 단어 문장 조회
    List<LearningContent> findAllByContentType(ContentType contentType);

    // 전체
    Slice<LearningContent> findAllByContentTypeAndDifficulty(
            ContentType contentType, Difficulty difficulty, Pageable pageable);

    // 특정 콘텐츠들만 (학습 완료 / 오답)
    Slice<LearningContent> findAllByContentTypeAndDifficultyAndContentIdIn(
            ContentType contentType, Difficulty difficulty, List<Long> contentIds, Pageable pageable);

    // 특정 콘텐츠 제외 (미학습)
    Slice<LearningContent> findAllByContentTypeAndDifficultyAndContentIdNotIn(
            ContentType contentType, Difficulty difficulty, List<Long> contentIds, Pageable pageable);
}
