package com.daehanforeigner.capstone.domain.learning_content.dto.admin;

import com.daehanforeigner.capstone.domain.user.entity.NativeLanguage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "언어별 번역 — 회원의 모국어와 일치하는 항목이 학습 화면에 표시됩니다")
public record TranslationRequestDTO(

        @Schema(description = "번역 언어", example = "EN", allowableValues = {"KR", "EN", "JP", "CN"})
        @NotNull(message = "번역 언어는 필수입니다.")
        NativeLanguage language,

        @Schema(description = "단어·문장의 뜻", example = "apple")
        String meaning,

        @Schema(description = "발음 도움말", example = "Open your mouth wide and say 'sa'.")
        String pronunciationGuide,

        @Schema(description = "모국어 발음 표기", example = "sa-gwa")
        String nativePronunciation
) {
}
