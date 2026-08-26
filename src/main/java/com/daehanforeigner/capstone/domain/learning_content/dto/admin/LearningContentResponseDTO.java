package com.daehanforeigner.capstone.domain.learning_content.dto.admin;

import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContentTranslation;
import com.daehanforeigner.capstone.domain.standard_pronunciation.entity.StandardPronunciation;
import com.daehanforeigner.capstone.domain.user.entity.NativeLanguage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "관리자 학습 콘텐츠 목록 항목")
public record LearningContentResponseDTO(
        @Schema(description = "학습 콘텐츠 ID", example = "10")
        Long learningContentId,

        @Schema(description = "카테고리 ID", example = "1")
        Long categoryId,

        @Schema(description = "카테고리 이름", example = "단어 학습")
        String categoryName,

        @Schema(description = "콘텐츠 유형", example = "WORD",
                allowableValues = {"SYLLABLE", "WORD", "SENTENCE"})
        ContentType contentType,

        @Schema(description = "난이도 — 난이도 구분이 없는 콘텐츠는 null", example = "BEGINNER",
                allowableValues = {"BEGINNER", "INTERMEDIATE", "ADVANCED"}, nullable = true)
        Difficulty difficulty,

        @Schema(description = "학습할 단어·문장", example = "사과")
        String text,

        @Schema(description = "예문", example = "사과를 먹었다.")
        String exampleSentence,

        @Schema(description = "표준 발음 표기", example = "[사과]")
        String standardPronunciationText,

        @Schema(description = "등록된 언어별 번역 — 등록하지 않은 언어는 목록에 없습니다")
        List<Translation> translations,

        @Schema(description = "원어민 음성 URL — 자료 미등록 시 null",
                example = "https://kr.object.ncloudstorage.com/버킷/audio/uuid.m4a")
        String answerAudioUrl,

        @Schema(description = "원어민 영상 URL — 자료 미등록 시 null",
                example = "https://kr.object.ncloudstorage.com/버킷/video/uuid.mp4")
        String answerVideoUrl

) {
    @Schema(description = "언어별 번역")
    public record Translation(
            @Schema(description = "번역 언어", example = "EN", allowableValues = {"KR", "EN", "JP", "CN"})
            NativeLanguage language,

            @Schema(description = "단어·문장의 뜻", example = "apple")
            String meaning,

            @Schema(description = "발음 도움말", example = "Open your mouth wide and say 'sa'.")
            String pronunciationGuide,

            @Schema(description = "모국어 발음 표기", example = "sa-gwa")
            String nativePronunciation
    ) {
    }

    public static LearningContentResponseDTO from(LearningContent learningContent,
                                                  StandardPronunciation pronunciation,
                                                  List<LearningContentTranslation> translations) {
        List<Translation> translationList = translations.stream()
                .map(translation -> new Translation(
                        translation.getLanguage(),
                        translation.getMeaning(),
                        translation.getPronunciationGuide(),
                        translation.getNativePronunciation()))
                .toList();

        return new LearningContentResponseDTO(
                learningContent.getContentId(),
                learningContent.getContentCategory() != null ? learningContent.getContentCategory().getCategoryId() : null,
                learningContent.getContentCategory() != null ? learningContent.getContentCategory().getName() : null,
                learningContent.getContentType(),
                learningContent.getDifficulty(),
                learningContent.getText(),
                learningContent.getExampleSentence(),
                learningContent.getStandardPronunciationText(),
                translationList,
                pronunciation != null ? pronunciation.getAnswerAudioUrl() : null,
                pronunciation != null ? pronunciation.getAnswerVideoUrl() : null
        );
    }
}
