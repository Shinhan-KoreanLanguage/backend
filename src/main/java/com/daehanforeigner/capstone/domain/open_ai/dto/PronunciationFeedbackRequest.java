package com.daehanforeigner.capstone.domain.open_ai.dto;

public record PronunciationFeedbackRequest(
        String recognizedText,
        double accuracy,
        int lipScore,
        int voiceScore
) {
}
