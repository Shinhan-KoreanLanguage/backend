package com.daehanforeigner.capstone.domain.open_ai.dto;

public record PronunciationFeedbackResult(
        RatingComment accuracy,
        RatingComment intonation,
        RatingComment duration,
        String tip
) {
    public record RatingComment(String rating, String comment) {
    }
}
