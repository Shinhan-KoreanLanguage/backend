package com.daehanforeigner.capstone.domain.learning_content.dto.admin;

import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.standard_pronunciation.entity.StandardPronunciation;

public record LearningContentResponseDTO(
        Long learningContentId, // 학습 컨텐츠 ID

        Long categoryId, // 카테고리 ID

        String categoryName, // 카테고리 이름

        ContentType contentType, // 컨텐츠 유형 (음절, 단어, 문장)

        Difficulty difficulty, // 난이도

        String text, // 학습 단어 or 문장

        String meaning, // 의미

        String exampleSentence, // 예문

        String pronunciationGuide, // 발음 가이드

        String standardPronunciationText, // 표준 발음 표기

        String nativePronunciation, // 모국어 발음 표기

        String answerAudioUrl, // 정답 오디오 URL

        String answerVideoUrl // 정답 비디오 URL

) {
    public static LearningContentResponseDTO from(LearningContent learningContent, StandardPronunciation pronunciation) {
        return new LearningContentResponseDTO(
                learningContent.getContentId(),
                learningContent.getContentCategory() != null ? learningContent.getContentCategory().getCategoryId() : null,
                learningContent.getContentCategory() != null ? learningContent.getContentCategory().getName() : null,
                learningContent.getContentType(),
                learningContent.getDifficulty(),
                learningContent.getText(),
                learningContent.getMeaning(),
                learningContent.getExampleSentence(),
                learningContent.getPronunciationGuide(),
                learningContent.getStandardPronunciationText(),
                learningContent.getNativePronunciation(),
                pronunciation != null ? pronunciation.getAnswerAudioUrl() : null,
                pronunciation != null ? pronunciation.getAnswerVideoUrl() : null
        );
    }
    }
}
