package com.daehanforeigner.capstone.domain.learning_content.dto.user;

import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.standard_pronunciation.entity.StandardPronunciation;

public record StudyContentResponseDTO(
        Long contentId,

        String categoryName,

        ContentType contentType,

        Difficulty difficulty,

        String text,

        String standardPronunciationText,

        String nativePronunciation,

        String meaning,

        String exampleSentence,

        String pronunciationGuide,

        String answerAudioUrl,

        String answerVideoUrl
) {
    public static StudyContentResponseDTO from(LearningContent content, StandardPronunciation standardPronunciation) {
        return new StudyContentResponseDTO(
                content.getContentId(),
                content.getContentCategory() != null ? content.getContentCategory().getName() : null,
                content.getContentType(),
                content.getDifficulty(),
                content.getText(),
                content.getStandardPronunciationText(), // 표기는 LearningContent 소속
                content.getNativePronunciation(),
                content.getMeaning(),
                content.getExampleSentence(),
                content.getPronunciationGuide(),
                // 발음 자료가 아직 등록되지 않은 콘텐츠는 null일 수 있다
                standardPronunciation != null ? standardPronunciation.getAnswerAudioUrl() : null,
                standardPronunciation != null ? standardPronunciation.getAnswerVideoUrl() : null
        );
    }
}