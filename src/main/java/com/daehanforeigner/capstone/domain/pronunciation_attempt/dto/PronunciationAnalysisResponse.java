package com.daehanforeigner.capstone.domain.pronunciation_attempt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PronunciationAnalysisResponse(
        @JsonProperty("recognized_text") String recognizedText,
        @JsonProperty("stt_accuracy") double sttAccuracy,
        @JsonProperty("mouth_accuracy") Double mouthAccuracy,
        @JsonProperty("final_accuracy") double finalAccuracy,
        @JsonProperty("is_correct") boolean isCorrect
) {
}
