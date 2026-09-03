package com.daehanforeigner.capstone.domain.learning_content.dto.admin;

import com.daehanforeigner.capstone.domain.user.entity.NativeLanguage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "언어별 번역 — 회원의 모국어와 일치하는 항목이 학습 화면에 표시됩니다")
public record TranslationRequestDTO(

        @Schema(description = "번역 언어", example = "EN", allowableValues = {"KR", "EN", "JP", "CN"})
        @NotNull(message = "번역 언어는 필수입니다.")
        NativeLanguage language,

        @Schema(description = "단어·문장의 뜻", example = "apple")
        @NotBlank(message = "뜻은 필수입니다.")
        String meaning,

        @Schema(description = "발음 도움말", example = "Open your mouth wide and say 'sa'.")
        @NotBlank(message = "발음 도움말은 필수입니다.")
        String pronunciationGuide,

        @Schema(description = """
                모국어 발음 표기. **KR은 생략할 수 있고, EN · JP · CN은 필수입니다.**

                KR을 생략하면 콘텐츠의 `standardPronunciationText`(한국어 표준 발음 표기)로 채워지고,
                반대로 `standardPronunciationText`를 생략하면 KR 값으로 채워집니다.
                둘 다 비우면 400 `PRONUNCIATION_TEXT_REQUIRED`가 반환됩니다.
                """,
                example = "sa-gwa", nullable = true)
        String nativePronunciation
) {
}
