package com.daehanforeigner.capstone.domain.open_ai.service;

import com.daehanforeigner.capstone.domain.open_ai.dto.OpenAIMessage;
import com.daehanforeigner.capstone.domain.open_ai.dto.OpenAIRequestDto;
import com.daehanforeigner.capstone.domain.open_ai.dto.OpenAIResponse;
import com.daehanforeigner.capstone.domain.user.entity.NativeLanguage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    private static final Map<NativeLanguage, String> LANGUAGE_NAMES = Map.of(
            NativeLanguage.KR, "한국어",
            NativeLanguage.EN, "영어",
            NativeLanguage.JP, "일본어",
            NativeLanguage.CN, "중국어"
    );

    private static final int MAX_TOKENS = 200;

    private final RestTemplate openAiRestTemplate;

    @Value("${openai.api-url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    public String generatePronunciationFeedback(String recognizedText, double accuracy, int lipScore, int voiceScore, NativeLanguage language) {
        String systemPrompt = """
                너는 한국어 발음 교정 코치야. 학습자의 발음 분석 결과(인식된 발화, 목표 문장, 통합 점수,
                입모양 점수, 음성 점수)를 보고 아래 세 항목을 각각 평가해서 반드시 JSON으로만 응답해.
                설명, 마크다운, 코드블록 없이 JSON 객체 하나만 출력해.

                평가 항목:
                - accuracy (정확도): 인식된 발화가 목표 문장과 얼마나 일치하는지
                - intonation (억양): 음절/단어별 억양(피치) 곡선이 자연스러운지
                - duration (발음 길이): 발화 속도·음절 길이가 적절한지 (너무 빠르거나 길게 끌지 않는지)

                각 항목의 rating은 "GOOD"(좋아요), "NORMAL"(보통), "BAD"(아쉬워요) 중 하나만 사용해 (이 값 자체는 영어로 고정).
                comment는 한 문장, 존댓말, 40자 이내로 구체적으로 작성해 (예: 몇 번째 음절의 어떤 부분이
                어떻게 아쉬운지). 문제가 없으면 "대부분의 발음이 정확해요!" 처럼 짧게 칭찬해.
                tip은 comment에서 지적한 문제를 고치기 위한 실전 팁 1개를 2문장 이내로 작성해.
                comment와 tip은 반드시 %s로만 작성해.

                반드시 아래 JSON 스키마를 그대로 따라:
                {
                  "accuracy": { "rating": "GOOD" | "NORMAL" | "BAD", "comment": "string" },
                  "intonation": { "rating": "GOOD" | "NORMAL" | "BAD", "comment": "string" },
                  "duration": { "rating": "GOOD" | "NORMAL" | "BAD", "comment": "string" },
                  "tip": "string"
                }
                """.formatted(LANGUAGE_NAMES.get(language));

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

        OpenAIRequestDto request = new OpenAIRequestDto(model, messages, MAX_TOKENS);

        OpenAIResponse response = openAiRestTemplate.postForObject(apiUrl, request, OpenAIResponse.class);

        return response.choices().get(0).message().content();
    }
}
