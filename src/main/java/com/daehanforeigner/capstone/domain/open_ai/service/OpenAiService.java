package com.daehanforeigner.capstone.domain.open_ai.service;

import com.daehanforeigner.capstone.domain.open_ai.dto.OpenAIMessage;
import com.daehanforeigner.capstone.domain.open_ai.dto.OpenAIRequestDto;
import com.daehanforeigner.capstone.domain.open_ai.dto.OpenAIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final RestTemplate restTemplate;

    @Value("${openai.api-url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    public String generatePronunciationFeedback(String recognizedText, double accuracy, int lipScore, int voiceScore) {
        String systemPrompt = """
                너는 한국어 발음 교정 코치야. 사용자의 발음 분석 결과(인식된 발화, 통합 점수, 입모양 점수, 음성 점수)를 보고
                어떤 부분이 부족한지와 어떻게 개선하면 좋을지 짧고 친절한 한국어 조언으로 알려줘.
                """;

        String userPrompt = """
                인식된 발화: %s
                통합 점수: %.1f
                입모양(mediapipe) 점수: %d
                음성(STT) 점수: %d
                """.formatted(recognizedText, accuracy, lipScore, voiceScore);

        List<OpenAIMessage> messages = List.of(
                new OpenAIMessage("system", systemPrompt),
                new OpenAIMessage("user", userPrompt)
        );

        OpenAIRequestDto request = new OpenAIRequestDto(model, messages);

        OpenAIResponse response = restTemplate.postForObject(apiUrl, request, OpenAIResponse.class);

        return response.choices().get(0).message().content();
    }
}
