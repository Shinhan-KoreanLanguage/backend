package com.daehanforeigner.capstone.domain.learning_content.repository;

import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContentTranslation;
import com.daehanforeigner.capstone.domain.user.entity.NativeLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface LearningContentTranslationRepository extends JpaRepository<LearningContentTranslation, Long> {

    List<LearningContentTranslation> findAllByLearningContent(LearningContent learningContent);

    // 목록 조회용 — 콘텐츠마다 조회하면 N + 1이 되므로 IN 절로 한 번에 가져온다.
    // 대체 언어(영어)까지 함께 받아야 번역이 없는 콘텐츠도 채울 수 있어 language도 IN으로 받는다
    List<LearningContentTranslation> findAllByLearningContentInAndLanguageIn(
            List<LearningContent> learningContents, Collection<NativeLanguage> languages);

    void deleteAllByLearningContent(LearningContent learningContent);

    void deleteAllByLearningContentIn(List<LearningContent> learningContents);
}
