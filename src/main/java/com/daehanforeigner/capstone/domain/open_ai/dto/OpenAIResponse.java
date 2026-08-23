package com.daehanforeigner.capstone.domain.open_ai.dto;

import java.util.List;

public record OpenAIResponse(List<Choice> choices) {
    public record Choice(OpenAIMessage message) {}
}

