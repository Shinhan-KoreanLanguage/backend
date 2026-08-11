package com.daehanforeigner.capstone.domain.open_ai.dto;

public record PronunciationFeedbackRequest(
        String recognizedText,
        double sttAccuracy,
        Double pitchAccuracy,
        boolean lengthMismatch
) {
}
