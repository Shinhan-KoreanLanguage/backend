package com.daehanforeigner.capstone.domain.pronunciation_attempt.dto;

import com.daehanforeigner.capstone.domain.audio_file.entity.AudioFile;
import com.daehanforeigner.capstone.domain.feedback.entity.Feedback;
import com.daehanforeigner.capstone.domain.feedback.entity.FeedbackLevel;
import com.daehanforeigner.capstone.domain.feedback.entity.FeedbackType;
import com.daehanforeigner.capstone.domain.phoneme_score.entity.PhonemeScore;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.entity.PronunciationAttempt;
import com.daehanforeigner.capstone.domain.standard_pronunciation.entity.StandardPronunciation;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

// 발음 피드백 화면에 필요한 분석 결과 전체
@Schema(description = "발음 분석 결과 — 피드백 화면 구성에 필요한 값 전체")
public record AttemptResultResponseDTO(
        @Schema(description = "발음 시도 ID", example = "42")
        Long attemptId,

        @Schema(description = "학습 콘텐츠 ID", example = "10")
        Long contentId,

        @Schema(description = "학습 텍스트 (정답)", example = "사과")
        String text, // 학습 텍스트

        @Schema(description = "STT가 인식한 사용자 발화 — 정답과 비교해 보여주면 이해에 도움됩니다",
                example = "사가")
        String recognizedText, // STT 인식 결과

        @Schema(description = "발음 점수 (0~100)", example = "82.5")
        Double voiceScore, // 발음 점수

        @Schema(description = "입모양 점수 (0~100) — **영상 미전송 시 null**", example = "91.0")
        Double lipScore, // 입모양 점수 (영상 미전송 시 null)

        @Schema(description = "억양 점수 (0~100)", example = "76.3")
        Double pitchScore, // 억양 점수

        @Schema(description = "종합 점수 (0~100) — 화면 상단 큰 숫자", example = "84.2")
        double accuracy, // 종합 점수 (화면 상단 큰 숫자)

        @Schema(description = "통과 여부 — 종합 점수 85% 이상이면 true", example = "false")
        boolean isPassed,

        // 아래 3개는 JSON 문자열이므로 프론트에서 JSON.parse 후 사용
        @Schema(description = """
                사용자 피치 곡선. **JSON 문자열이므로 JSON.parse() 후 사용하세요.**
                파싱하면 `[{ "time": 0.544, "pitch": 1.05 }, ...]` 형태입니다.
                """,
                example = "[{\"time\":0.544,\"pitch\":1.05}]")
        String userPitchData, // 사용자 피치 곡선

        @Schema(description = "원어민 피치 곡선 (비교용). 형식은 userPitchData와 동일하며 JSON.parse() 필요",
                example = "[{\"time\":0.512,\"pitch\":1.12}]")
        String nativePitchData, // 원어민 피치 곡선 (비교용)

        @Schema(description = "억양이 어긋난 구간 (그래프 강조 표시용). JSON.parse() 필요",
                example = "[{\"start\":0.8,\"end\":1.2}]")
        String pitchHighlightSegments, // 억양이 어긋난 구간 (강조 표시용)

        @Schema(description = "발음 길이가 원어민과 크게 다른지 여부 — true면 '너무 빠르게/느리게' 안내",
                example = "false")
        Boolean lengthMismatch,

        @Schema(description = "사용자 녹음 URL (파형 렌더링·다시 듣기용)",
                example = "http://localhost:8080/images/audio/uuid.webm")
        String userAudioUrl, // 사용자 녹음 (파형 렌더링용)

        @Schema(description = "원어민 음성 URL (파형 비교용)",
                example = "http://localhost:8080/images/audio/uuid.m4a")
        String answerAudioUrl, // 원어민 음성 (파형 비교용)

        @Schema(description = "음절별 점수 — 파형을 음절 단위로 나눠 표시할 때 사용")
        List<SyllableScore> syllableScores,

        @Schema(description = "상세 피드백 카드 3장 (정확도 · 억양 · 발음 길이)")
        List<FeedbackItem> feedbacks
) {
    // 파형의 음절 구간 분할에 사용
    @Schema(description = "음절별 점수")
    public record SyllableScore(
            @Schema(description = "음절", example = "사")
            String syllable,

            @Schema(description = "해당 음절 점수 (0~100)", example = "88.0")
            Double score,

            @Schema(description = "취약 음절 여부 — true면 빨갛게 강조하고 재연습을 추천", example = "false")
            Boolean isWeak,

            @Schema(description = "구간 시작 시각 (초)", example = "0.544")
            Double startTime,

            @Schema(description = "구간 끝 시각 (초)", example = "1.344")
            Double endTime) {
    }

    // 화면 우측 상세 피드백 카드 3장
    @Schema(description = "피드백 항목")
    public record FeedbackItem(
            @Schema(description = "피드백 항목 — ACCURACY(정확도) · INTONATION(억양) · LENGTH(발음 길이)",
                    example = "ACCURACY", allowableValues = {"ACCURACY", "INTONATION", "LENGTH"})
            FeedbackType type,

            @Schema(description = "등급 — GOOD(좋음) · NORMAL(보통) · WEAK(개선 필요). 카드 색상에 사용하세요",
                    example = "NORMAL", allowableValues = {"GOOD", "NORMAL", "WEAK"})
            FeedbackLevel level,

            @Schema(description = "사용자에게 보여줄 피드백 문구",
                    example = "발음이 대체로 정확해요. 조금만 더 또렷하게 발음해 보세요.")
            String content) {
    }

    public static AttemptResultResponseDTO from(PronunciationAttempt attempt,
                                                AudioFile audioFile,
                                                StandardPronunciation pronunciation,
                                                List<PhonemeScore> phonemeScores,
                                                List<Feedback> feedbacks) {
        return new AttemptResultResponseDTO(
                attempt.getAttemptId(),
                attempt.getLearningContent().getContentId(),
                attempt.getLearningContent().getText(),
                attempt.getRecognizedText(),
                attempt.getVoiceScore(),
                attempt.getLipScore(),
                attempt.getPitchScore(),
                attempt.getAccuracy(),
                attempt.isPassed(),
                attempt.getUserPitchData(),
                pronunciation != null ? pronunciation.getNativePitchData() : null,
                attempt.getPitchHighlightSegments(),
                attempt.getLengthMismatch(),
                audioFile != null ? audioFile.getFileUrl() : null,
                pronunciation != null ? pronunciation.getAnswerAudioUrl() : null,
                phonemeScores.stream()
                        .map(score -> new SyllableScore(score.getPhoneme(), score.getScore(),
                                score.getIsWeak(), score.getStartTime(), score.getEndTime()))
                        .toList(),
                feedbacks.stream()
                        .map(feedback -> new FeedbackItem(feedback.getFeedbackType(),
                                feedback.getLevel(), feedback.getContent()))
                        .toList());
    }
}
