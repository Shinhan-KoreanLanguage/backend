package com.daehanforeigner.capstone.domain.pronunciation_attempt.dto;

import com.daehanforeigner.capstone.domain.audio_file.entity.AudioFile;
import com.daehanforeigner.capstone.domain.feedback.entity.Feedback;
import com.daehanforeigner.capstone.domain.feedback.entity.FeedbackLevel;
import com.daehanforeigner.capstone.domain.feedback.entity.FeedbackType;
import com.daehanforeigner.capstone.domain.phoneme_score.entity.PhonemeScore;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.entity.PronunciationAttempt;
import com.daehanforeigner.capstone.domain.standard_pronunciation.entity.StandardPronunciation;

import java.util.List;

// 발음 피드백 화면에 필요한 분석 결과 전체
public record AttemptResultResponseDTO(
        Long attemptId,

        Long contentId,

        String text, // 학습 텍스트

        String recognizedText, // STT 인식 결과

        Double voiceScore, // 발음 점수

        Double lipScore, // 입모양 점수 (영상 미전송 시 null)

        Double pitchScore, // 억양 점수

        double accuracy, // 종합 점수 (화면 상단 큰 숫자)

        boolean isPassed,

        // 아래 3개는 JSON 문자열이므로 프론트에서 JSON.parse 후 사용
        String userPitchData, // 사용자 피치 곡선

        String nativePitchData, // 원어민 피치 곡선 (비교용)

        String pitchHighlightSegments, // 억양이 어긋난 구간 (강조 표시용)

        Boolean lengthMismatch,

        String userAudioUrl, // 사용자 녹음 (파형 렌더링용)

        String answerAudioUrl, // 원어민 음성 (파형 비교용)

        List<SyllableScore> syllableScores,

        List<FeedbackItem> feedbacks
) {
    // 파형의 음절 구간 분할에 사용
    public record SyllableScore(String syllable, Double score, Boolean isWeak,
                                Double startTime, Double endTime) {
    }

    // 화면 우측 상세 피드백 카드 3장
    public record FeedbackItem(FeedbackType type, FeedbackLevel level, String content) {
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