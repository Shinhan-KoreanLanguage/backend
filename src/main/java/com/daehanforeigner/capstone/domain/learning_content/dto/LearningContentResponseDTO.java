package com.daehanforeigner.capstone.domain.learning_content.dto;

import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;

public record LearningContentResponseDTO(
        Long learningContentId, // 학습 컨텐츠 ID

        ContentType contentType, // 컨텐츠 유형 (음절, 단어, 문장)

        Difficulty difficulty, // 난이도

        String text, // 학습 단어 or 문장

        String meaning, // 의미

        String exampleSentence, // 예문

        String pronunciationGuide // 발음 가이드
) {
    public static LearningContentResponseDTO from(LearningContent learningContent) {
        return new LearningContentResponseDTO(
                learningContent.getContentId(),
                learningContent.getContentType(),
                learningContent.getDifficulty(),
                learningContent.getText(),
                learningContent.getMeaning(),
                learningContent.getExampleSentence(),
                learningContent.getPronunciationGuide()
        );
    }
}
