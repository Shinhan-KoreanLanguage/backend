package com.daehanforeigner.capstone.domain.pronunciation_attempt.dto;

public record PronunciationResultResponse(
        String recognizedText,
        double accuracy,
        boolean isCorrect,
        String feedback
) {
}
