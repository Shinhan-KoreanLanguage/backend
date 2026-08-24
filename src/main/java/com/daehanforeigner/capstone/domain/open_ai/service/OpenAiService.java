package com.daehanforeigner.capstone.domain.open_ai.service;

import com.daehanforeigner.capstone.domain.open_ai.dto.OpenAIMessage;
import com.daehanforeigner.capstone.domain.open_ai.dto.OpenAIRequestDto;
import com.daehanforeigner.capstone.domain.open_ai.dto.OpenAIResponse;
import com.daehanforeigner.capstone.domain.open_ai.dto.PronunciationFeedbackResult;
import com.daehanforeigner.capstone.domain.open_ai.dto.PronunciationFeedbackResult.RatingComment;
import com.daehanforeigner.capstone.domain.user.entity.NativeLanguage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Slf4j
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

    private final ObjectMapper objectMapper;

    @Value("${openai.api-url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    public PronunciationFeedbackResult generatePronunciationFeedback(
            String recognizedText, double sttAccuracy, Double pitchAccuracy, boolean lengthMismatch, NativeLanguage language) {

        String systemPrompt = """
                너는 한국어 발음 교정 코치야. 학습자의 발음 분석 결과(인식된 발화, 음성 정확도,
                억양 정확도, 발음 길이 불일치 여부)를 보고 아래 세 항목을 각각 평가해서 반드시 JSON으로만 응답해.
                설명, 마크다운, 코드블록 없이 JSON 객체 하나만 출력해.

                평가 항목:
                - accuracy (정확도): 음성 정확도 점수를 근거로 발음이 목표 문장과 얼마나 일치하는지
                - intonation (억양): 억양 정확도 점수를 근거로 억양(피치) 곡선이 자연스러운지
                - duration (발음 길이): 발음 길이 불일치 여부를 근거로 발화 속도·음절 길이가 적절한지

                각 항목의 rating은 "GOOD"(좋아요), "NORMAL"(보통), "BAD"(아쉬워요) 중 하나만 사용해 (이 값 자체는 영어로 고정).
                comment는 한 문장, 존댓말, 40자 이내로 구체적으로 작성해. 문제가 없으면
                "대부분의 발음이 정확해요!" 처럼 짧게 칭찬해.
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
                음성 정확도(STT): %.1f
                억양 정확도(피치): %s
                발음 길이가 원어민과 크게 다름: %s
                """.formatted(recognizedText, sttAccuracy,
                pitchAccuracy != null ? String.format("%.1f", pitchAccuracy) : "정보 없음",
                lengthMismatch ? "예" : "아니오");

        List<OpenAIMessage> messages = List.of(
                new OpenAIMessage("system", systemPrompt),
                new OpenAIMessage("user", userPrompt)
        );

        OpenAIRequestDto request = new OpenAIRequestDto(model, messages, MAX_TOKENS);

        try {
            OpenAIResponse response = openAiRestTemplate.postForObject(apiUrl, request, OpenAIResponse.class);
            String content = response.choices().get(0).message().content();
            return objectMapper.readValue(content, PronunciationFeedbackResult.class);
        } catch (Exception e) {
            // OpenAI 호출 실패(네트워크 오류·타임아웃·요금 한도·JSON 파싱 실패 등) 시
            // 발음 시도 저장 전체가 롤백되지 않도록 점수 기반 규칙 피드백으로 대체한다
            log.warn("OpenAI 피드백 생성 실패, fallback 피드백으로 대체합니다. sttAccuracy={}, pitchAccuracy={}",
                    sttAccuracy, pitchAccuracy, e);
            return buildFallbackFeedback(sttAccuracy, pitchAccuracy, lengthMismatch, language);
        }
    }

    // 등급 임계값 — 게임 합격 기준(85%)과 동일한 기준을 사용한다
    private static final double GOOD_THRESHOLD = 85.0;
    private static final double NORMAL_THRESHOLD = 60.0;

    private static final Map<NativeLanguage, String[]> ACCURACY_COMMENTS = Map.of(
            NativeLanguage.KR, new String[]{"발음이 목표 문장과 매우 비슷해요!", "발음이 대체로 잘 맞아요.", "목표 문장과 발음 차이가 커요."},
            NativeLanguage.EN, new String[]{"Your pronunciation closely matches the target.", "Your pronunciation mostly matches the target.", "Your pronunciation differs a lot from the target."},
            NativeLanguage.JP, new String[]{"発音は目標の文にとても近いです!", "発音はおおむね合っています。", "目標の文と発音の差が大きいです。"},
            NativeLanguage.CN, new String[]{"你的发音非常接近目标句子!", "发音基本准确。", "发音与目标句子差异较大。"}
    );

    private static final Map<NativeLanguage, String[]> INTONATION_COMMENTS = Map.of(
            NativeLanguage.KR, new String[]{"억양이 원어민과 매우 비슷해요!", "억양이 대체로 자연스러워요.", "억양이 원어민과 차이가 커요."},
            NativeLanguage.EN, new String[]{"Your intonation closely matches a native speaker's.", "Your intonation is mostly natural.", "Your intonation differs a lot from a native speaker's."},
            NativeLanguage.JP, new String[]{"イントネーションがネイティブにとても近いです!", "イントネーションはおおむね自然です。", "イントネーションがネイティブと大きく違います。"},
            NativeLanguage.CN, new String[]{"你的语调非常接近母语者!", "语调基本自然。", "语调与母语者差异较大。"}
    );

    // 발음 길이는 STT 서버가 boolean(lengthMismatch)만 넘겨주므로 2단계로만 판정한다
    private static final Map<NativeLanguage, String[]> DURATION_COMMENTS = Map.of(
            NativeLanguage.KR, new String[]{"발음 길이가 원어민과 비슷해요.", "발음 길이가 원어민과 차이가 커요."},
            NativeLanguage.EN, new String[]{"Your pronunciation length is close to a native speaker's.", "Your pronunciation length differs a lot from a native speaker's."},
            NativeLanguage.JP, new String[]{"発音の長さがネイティブに近いです。", "発音の長さがネイティブと大きく違います。"},
            NativeLanguage.CN, new String[]{"你的发音时长接近母语者。", "你的发音时长与母语者差异较大。"}
    );

    private static final Map<NativeLanguage, String> FALLBACK_TIPS = Map.of(
            NativeLanguage.KR, "천천히, 또박또박 다시 한 번 따라 말해보세요.",
            NativeLanguage.EN, "Try saying it again slowly and clearly.",
            NativeLanguage.JP, "もう一度、ゆっくりはっきり言ってみましょう。",
            NativeLanguage.CN, "请再慢慢、清楚地跟读一遍。"
    );

    private PronunciationFeedbackResult buildFallbackFeedback(
            double sttAccuracy, Double pitchAccuracy, boolean lengthMismatch, NativeLanguage language) {

        int accuracyIdx = ratingIndex(sttAccuracy);
        // 억양 데이터가 없으면(영상 미제출 등) 판정할 근거가 없으므로 중립(NORMAL)으로 둔다
        int intonationIdx = pitchAccuracy != null ? ratingIndex(pitchAccuracy) : 1;
        int durationIdx = lengthMismatch ? 1 : 0;

        RatingComment accuracy = new RatingComment(RATING_NAMES[accuracyIdx], ACCURACY_COMMENTS.get(language)[accuracyIdx]);
        RatingComment intonation = new RatingComment(RATING_NAMES[intonationIdx], INTONATION_COMMENTS.get(language)[intonationIdx]);
        RatingComment duration = new RatingComment(durationIdx == 0 ? "GOOD" : "BAD", DURATION_COMMENTS.get(language)[durationIdx]);

        return new PronunciationFeedbackResult(accuracy, intonation, duration, FALLBACK_TIPS.get(language));
    }

    private static final String[] RATING_NAMES = {"GOOD", "NORMAL", "BAD"};

    private int ratingIndex(double score) {
        if (score >= GOOD_THRESHOLD) return 0;
        if (score >= NORMAL_THRESHOLD) return 1;
        return 2;
    }
}
