package com.daehanforeigner.capstone.domain.learning_content.dto.user;

import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContentTranslation;
import com.daehanforeigner.capstone.domain.standard_pronunciation.entity.StandardPronunciation;
import com.daehanforeigner.capstone.domain.user.entity.NativeLanguage;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "발음 연습 화면용 학습 콘텐츠 상세")
public record StudyContentResponseDTO(
        @Schema(description = "학습 콘텐츠 ID", example = "10")
        Long contentId,

        @Schema(description = "카테고리 이름", example = "단어 학습")
        String categoryName,

        @Schema(description = "콘텐츠 유형", example = "WORD",
                allowableValues = {"SYLLABLE", "WORD", "SENTENCE"})
        ContentType contentType,

        @Schema(description = "난이도 — 난이도 구분이 없는 콘텐츠는 null", example = "BEGINNER",
                allowableValues = {"BEGINNER", "INTERMEDIATE", "ADVANCED"}, nullable = true)
        Difficulty difficulty,

        @Schema(description = "학습할 단어·문장 (화면 중앙 큰 글씨)", example = "사과")
        String text,

        @Schema(description = "표준 발음 표기 (한국어)", example = "[사과]")
        String standardPronunciationText,

        @Schema(description = "실제로 내려간 번역의 언어 — 요청 회원의 모국어와 다르면 대체된 것입니다",
                example = "EN", allowableValues = {"KR", "EN", "JP", "CN"}, nullable = true)
        NativeLanguage translationLanguage,

        @Schema(description = "회원 모국어 발음 표기 — 번역 미등록 시 null", example = "sa-gwa", nullable = true)
        String nativePronunciation,

        @Schema(description = "회원 모국어로 된 뜻 — 번역 미등록 시 null", example = "apple", nullable = true)
        String meaning,

        @Schema(description = "예문 (한국어)", example = "사과를 먹었다.")
        String exampleSentence,

        @Schema(description = "회원 모국어로 된 발음 도움말 — 번역 미등록 시 null",
                example = "Open your mouth wide and say 'sa'.", nullable = true)
        String pronunciationGuide,

        @Schema(description = "원어민 음성 URL — 자료 미등록 시 null",
                example = "https://kr.object.ncloudstorage.com/버킷/audio/uuid.m4a")
        String answerAudioUrl,

        @Schema(description = "원어민 영상 URL — 자료 미등록 시 null",
                example = "https://kr.object.ncloudstorage.com/버킷/video/uuid.mp4")
        String answerVideoUrl
) {
    public static StudyContentResponseDTO from(LearningContent content,
                                               LearningContentTranslation translation,
                                               StandardPronunciation standardPronunciation) {
        return new StudyContentResponseDTO(
                content.getContentId(),
                content.getContentCategory() != null ? content.getContentCategory().getName() : null,
                content.getContentType(),
                content.getDifficulty(),
                content.getText(),
                content.getStandardPronunciationText(),
                // 번역이 등록되지 않은 콘텐츠일 수 있다
                translation != null ? translation.getLanguage() : null,
                translation != null ? translation.getNativePronunciation() : null,
                translation != null ? translation.getMeaning() : null,
                content.getExampleSentence(),
                translation != null ? translation.getPronunciationGuide() : null,
                // 발음 자료가 아직 등록되지 않은 콘텐츠는 null일 수 있다
                standardPronunciation != null ? standardPronunciation.getAnswerAudioUrl() : null,
                standardPronunciation != null ? standardPronunciation.getAnswerVideoUrl() : null
        );
    }
}
