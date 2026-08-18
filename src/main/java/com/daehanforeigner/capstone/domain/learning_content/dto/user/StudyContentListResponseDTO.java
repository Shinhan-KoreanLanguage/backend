package com.daehanforeigner.capstone.domain.learning_content.dto.user;

import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.learning_content.entity.StudyStatus;
import io.swagger.v3.oas.annotations.media.Schema;

// 학습 목록 카드 한 장에 필요한 값.
// 상세 조회(StudyContentResponseDTO)와 달리 의미·예문·영상은 담지 않는다 — 목록에서 쓰지 않는 값이다
@Schema(description = "학습 콘텐츠 목록 항목")
public record StudyContentListResponseDTO(
        @Schema(description = "학습 콘텐츠 ID", example = "10")
        Long contentId,

        @Schema(description = "카테고리 이름", example = "단어 학습")
        String categoryName,

        @Schema(description = "콘텐츠 유형", example = "WORD",
                allowableValues = {"SYLLABLE", "WORD", "SENTENCE"})
        ContentType contentType,

        @Schema(description = "난이도", example = "BEGINNER",
                allowableValues = {"BEGINNER", "INTERMEDIATE", "ADVANCED"})
        Difficulty difficulty,

        @Schema(description = "학습할 단어·문장", example = "사과")
        String text,

        @Schema(description = "표준 발음 표기", example = "[사과]")
        String standardPronunciationText,

        @Schema(description = "원어민 음성 URL — 카드에서 미리 듣기용. 자료 미등록 시 null",
                example = "http://localhost:8080/images/audio/uuid.m4a")
        String answerAudioUrl,

        @Schema(description = "회원 기준 학습 상태 — 카드 배지에 표시하세요", example = "WRONG",
                allowableValues = {"NOT_STARTED", "WRONG", "COMPLETED"})
        StudyStatus status
) {
    public static StudyContentListResponseDTO from(LearningContent content,
                                                   String answerAudioUrl,
                                                   StudyStatus status) {
        return new StudyContentListResponseDTO(
                content.getContentId(),
                // 카테고리는 nullable이므로 방어 (트러블슈팅 3번과 같은 유형)
                content.getContentCategory() != null ? content.getContentCategory().getName() : null,
                content.getContentType(),
                content.getDifficulty(),
                content.getText(),
                content.getStandardPronunciationText(),
                answerAudioUrl,
                status
        );
    }
}
