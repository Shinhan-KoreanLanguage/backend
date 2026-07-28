package com.daehanforeigner.capstone.domain.open_ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenAIRequestDto(
        String model,
        List<OpenAIMessage> messages,
        @JsonProperty("max_tokens") int maxTokens
) {
}
