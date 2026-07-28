package com.daehanforeigner.capstone.domain.learning_content.repository;

import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningContentRepository extends JpaRepository<LearningContent, Long> { // 단어 혹은 문장 학습 컨텐츠 조회

    List<LearningContent> findAllByContentType(ContentType contentType);

    List<LearningContent> findAllByContentTypeAndDifficulty(ContentType contentType, Difficulty difficulty); // 단어 혹은 문장 + 난이도 조회
}
