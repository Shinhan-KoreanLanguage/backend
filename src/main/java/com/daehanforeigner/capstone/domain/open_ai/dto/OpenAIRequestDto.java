package com.daehanforeigner.capstone.domain.open_ai.dto;

import java.util.List;

public record OpenAIRequestDto(String model, List<OpenAIMessage> messages) {
}
