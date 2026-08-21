package com.daehanforeigner.capstone.domain.pronunciation_attempt.service;

import com.daehanforeigner.capstone.domain.audio_file.entity.AudioFile;
import com.daehanforeigner.capstone.domain.audio_file.repository.AudioFileRepository;
import com.daehanforeigner.capstone.domain.feedback.entity.Feedback;
import com.daehanforeigner.capstone.domain.feedback.entity.FeedbackLevel;
import com.daehanforeigner.capstone.domain.feedback.entity.FeedbackType;
import com.daehanforeigner.capstone.domain.feedback.repository.FeedbackRepository;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.learning_content.repository.LearningContentRepository;
import com.daehanforeigner.capstone.domain.phoneme_score.entity.PhonemeScore;
import com.daehanforeigner.capstone.domain.phoneme_score.repository.PhonemeScoreRepository;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.dto.AttemptResultResponseDTO;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.entity.PronunciationAttempt;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.repository.PronunciationAttemptRepository;
import com.daehanforeigner.capstone.domain.standard_pronunciation.entity.StandardPronunciation;
import com.daehanforeigner.capstone.domain.standard_pronunciation.repository.StandardPronunciationRepository;
import com.daehanforeigner.capstone.domain.user.entity.User;
import com.daehanforeigner.capstone.domain.user.repository.UserRepository;
import com.daehanforeigner.capstone.domain.wrong_answer.entity.WrongAnswer;
import com.daehanforeigner.capstone.domain.wrong_answer.repository.WrongAnswerRepository;
import com.daehanforeigner.capstone.global.ai.PronunciationAiClient;
import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import com.daehanforeigner.capstone.global.storage.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

// 발음 시도(녹음 업로드 → AI 분석 → 결과 저장·조회) 담당
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PronunciationAttemptService {

    private final PronunciationAttemptRepository pronunciationAttemptRepository;

    private final AudioFileRepository audioFileRepository;

    private final PhonemeScoreRepository phonemeScoreRepository;

    private final FeedbackRepository feedbackRepository;

    private final WrongAnswerRepository wrongAnswerRepository;

    private final LearningContentRepository learningContentRepository;

    private final StandardPronunciationRepository standardPronunciationRepository;

    private final UserRepository userRepository;

    private final FileService fileService;

    private final PronunciationAiClient aiClient;

    // 녹음 종료 시 호출 — 파일 저장 → AI 분석 → 결과 저장
    @Transactional
    public Long createAttempt(Long userId, Long contentId,
                              MultipartFile audio, MultipartFile video, Integer durationMs) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LearningContent content = learningContentRepository.findById(contentId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_FOUND));

        // 오디오는 필수 — 없으면 분석 자체가 불가능하다
        if (audio == null || audio.isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_FILE);
        }

        // 1. 사용자 녹음·녹화 저장 (영상은 웹캠 거부 시 없을 수 있음)
        String audioUrl = fileService.saveAudio(audio, "attempt/audio");
        String videoUrl = (video != null && !video.isEmpty())
                ? fileService.saveVideo(video, "attempt/video")
                : null;

        // 2. AI 분석 — 방금 저장한 파일을 다시 읽어 전송한다.
        //    실패하면 예외가 던져지고 아래 저장이 전부 롤백된다
        JsonNode result = aiClient.analyze(
                content.getText(),
                fileService.loadAsResource(audioUrl),
                videoUrl != null ? fileService.loadAsResource(videoUrl) : null);

        JsonNode pitchCurve = result.path("pitch_curve");
        boolean passed = result.path("is_correct").asBoolean(false);

        // 시도 기록과 오답 기록 양쪽에 쓰이므로 한 번만 꺼내둔다
        double accuracy = result.path("final_accuracy").asDouble(0.0);

        // 3. 시도 기록 저장 (분석 결과를 담아 한 번에)
        PronunciationAttempt attempt = pronunciationAttemptRepository.save(
                PronunciationAttempt.builder()
                        .user(user)
                        .learningContent(content)
                        .recognizedText(textOrNull(result.path("recognized_text")))
                        .voiceScore(nullableDouble(result, "stt_accuracy"))
                        .lipScore(nullableDouble(result, "mouth_accuracy")) // 영상 없으면 null
                        .pitchScore(nullableDouble(result, "pitch_accuracy"))
                        .accuracy(accuracy)
                        .isPassed(passed)
                        .userPitchData(jsonOrNull(pitchCurve.path("user")))
                        .pitchHighlightSegments(jsonOrNull(pitchCurve.path("highlight_segments")))
                        .lengthMismatch(pitchCurve.path("length_mismatch").asBoolean(false))
                        .build());

        // 4. 업로드한 미디어 기록
        audioFileRepository.save(AudioFile.builder()
                .attempt(attempt)
                .fileUrl(audioUrl)
                .webcamRecordUrl(videoUrl)
                .fileSize((int) audio.getSize())
                .durationMs(durationMs != null ? durationMs : 0)
                .build());

        // 5. 음절별 점수 (파형 구간 분할·추천 연습에 사용)
        for (JsonNode syllable : result.path("syllable_scores")) {
            phonemeScoreRepository.save(PhonemeScore.builder()
                    .attempt(attempt)
                    .phoneme(textOrNull(syllable.path("syllable")))
                    .score(nullableDouble(syllable, "score"))
                    .isWeak(syllable.path("is_weak").asBoolean(false))
                    .startTime(nullableDouble(syllable, "start_time"))
                    .endTime(nullableDouble(syllable, "end_time"))
                    .build());
        }

        // 6. 피드백 3항목 생성 — AI는 점수만 주므로 문구·등급은 우리가 만든다
        saveFeedbacks(attempt, user, result);

        // 7. 오답 기록 갱신
        updateWrongAnswer(user, content, passed, accuracy);

        return attempt.getAttemptId();
    }

    // 피드백 화면용 결과 조회
    public AttemptResultResponseDTO getAttemptResult(Long userId, Long attemptId) {
        PronunciationAttempt attempt = pronunciationAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new CustomException(ErrorCode.ATTEMPT_NOT_FOUND));

        // 남의 시도는 존재 자체를 알리지 않도록 404로 응답한다
        if (!attempt.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ATTEMPT_NOT_FOUND);
        }

        // 원어민 음성·피치는 화면에서 사용자 것과 나란히 비교하는 데 쓰인다
        StandardPronunciation pronunciation = standardPronunciationRepository
                .findByLearningContent(attempt.getLearningContent())
                .orElse(null);

        return AttemptResultResponseDTO.from(
                attempt,
                audioFileRepository.findByAttempt(attempt).orElse(null),
                pronunciation,
                phonemeScoreRepository.findAllByAttempt(attempt),
                feedbackRepository.findAllByAttempt(attempt));
    }

    // 화면의 상세 피드백 3항목을 점수 구간으로 만들어 저장
    private void saveFeedbacks(PronunciationAttempt attempt, User user, JsonNode result) {
        double stt = result.path("stt_accuracy").asDouble(0.0);
        double pitch = result.path("pitch_accuracy").asDouble(0.0);
        boolean lengthMismatch = result.path("pitch_curve").path("length_mismatch").asBoolean(false);

        saveFeedback(attempt, user, FeedbackType.ACCURACY, levelOf(stt), accuracyMessage(levelOf(stt)));
        saveFeedback(attempt, user, FeedbackType.INTONATION, levelOf(pitch), intonationMessage(levelOf(pitch)));
        saveFeedback(attempt, user, FeedbackType.LENGTH,
                lengthMismatch ? FeedbackLevel.WEAK : FeedbackLevel.GOOD,
                lengthMismatch ? "발음 길이가 원어민과 많이 달라요. 천천히 따라 읽어 보세요." : "발음 길이가 적절해요!");
    }

    private String accuracyMessage(FeedbackLevel level) {
        return switch (level) {
            case GOOD -> "대부분의 발음이 정확해요!";
            case NORMAL -> "대체로 알아들을 수 있지만 일부 소리가 흐려요.";
            case WEAK -> "제시된 단어와 다르게 들려요. 또박또박 발음해 보세요.";
        };
    }

    private String intonationMessage(FeedbackLevel level) {
        return switch (level) {
            case GOOD -> "억양이 자연스러워요!";
            case NORMAL -> "억양이 조금 밋밋해요. 높낮이를 살려 보세요.";
            case WEAK -> "억양의 높낮이가 원어민과 많이 달라요.";
        };
    }

    private void saveFeedback(PronunciationAttempt attempt, User user,
                              FeedbackType type, FeedbackLevel level, String content) {
        feedbackRepository.save(Feedback.builder()
                .attempt(attempt)
                .feedbackType(type)
                .level(level)
                .content(content)
                .language(user.getNativeLanguage())
                .build());
    }

    private FeedbackLevel levelOf(double score) {
        if (score >= 80) {
            return FeedbackLevel.GOOD;
        }
        if (score >= 60) {
            return FeedbackLevel.NORMAL;
        }
        return FeedbackLevel.WEAK;
    }

    // 오답 기록은 회원·콘텐츠당 1건만 두고 갱신한다
    private void updateWrongAnswer(User user, LearningContent content, boolean passed, double accuracy) {
        wrongAnswerRepository.findByUserAndLearningContent(user, content)
                .ifPresentOrElse(
                        wrongAnswer -> wrongAnswer.recordAttempt(passed, accuracy), // 더티 체킹으로 반영
                        () -> {
                            // 처음부터 맞히면 오답 기록을 만들 필요가 없다
                            if (!passed) {
                                wrongAnswerRepository.save(WrongAnswer.builder()
                                        .user(user)
                                        .learningContent(content)
                                        .wrongCount(1)
                                        .isSolved(false)
                                        .lastAttemptedAt(LocalDateTime.now())
                                        .lastAccuracy(accuracy)
                                        .build());
                            }
                        });
    }

    // null이 올 수 있는 점수 (영상 미전송 시 mouth_accuracy 등)
    private Double nullableDouble(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return (value.isMissingNode() || value.isNull()) ? null : value.asDouble();
    }

    // JSON 컬럼에 넣을 값 — 필드가 없으면 빈 문자열 대신 null
    private String jsonOrNull(JsonNode node) {
        return (node.isMissingNode() || node.isNull()) ? null : node.toString();
    }

    private String textOrNull(JsonNode node) {
        return (node.isMissingNode() || node.isNull()) ? null : node.asText();
    }
}