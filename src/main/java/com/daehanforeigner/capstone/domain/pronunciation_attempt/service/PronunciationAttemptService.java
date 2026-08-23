package com.daehanforeigner.capstone.domain.pronunciation_attempt.service;

import com.daehanforeigner.capstone.domain.audio_file.entity.AudioFile;
import com.daehanforeigner.capstone.domain.audio_file.repository.AudioFileRepository;
import com.daehanforeigner.capstone.domain.feedback.entity.Feedback;
import com.daehanforeigner.capstone.domain.feedback.entity.FeedbackLevel;
import com.daehanforeigner.capstone.domain.feedback.entity.FeedbackType;
import com.daehanforeigner.capstone.domain.feedback.repository.FeedbackRepository;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.learning_content.repository.LearningContentRepository;
import com.daehanforeigner.capstone.domain.open_ai.dto.PronunciationFeedbackResult;
import com.daehanforeigner.capstone.domain.open_ai.service.OpenAiService;
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

    private final OpenAiService openAiService;

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

        // 3. 시도 기록 저장 (분석 결과를 담아 한 번에)
        PronunciationAttempt attempt = pronunciationAttemptRepository.save(
                PronunciationAttempt.builder()
                        .user(user)
                        .learningContent(content)
                        .recognizedText(textOrNull(result.path("recognized_text")))
                        .voiceScore(nullableDouble(result, "stt_accuracy"))
                        .lipScore(nullableDouble(result, "mouth_accuracy")) // 영상 없으면 null
                        .pitchScore(nullableDouble(result, "pitch_accuracy"))
                        .accuracy(result.path("final_accuracy").asDouble(0.0))
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

        // 6. 피드백 3항목 생성 — 점수를 GPT에 넘겨 등급·문구를 생성받는다
        saveFeedbacks(attempt, user, result);

        // 7. 오답 기록 갱신
        updateWrongAnswer(user, content, passed);

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

    // 화면의 상세 피드백 3항목 — GPT가 점수를 바탕으로 등급·문구를 직접 생성
    private void saveFeedbacks(PronunciationAttempt attempt, User user, JsonNode result) {
        double stt = result.path("stt_accuracy").asDouble(0.0);
        Double pitch = nullableDouble(result, "pitch_accuracy");
        boolean lengthMismatch = result.path("pitch_curve").path("length_mismatch").asBoolean(false);

        PronunciationFeedbackResult feedback = openAiService.generatePronunciationFeedback(
                attempt.getRecognizedText(), stt, pitch, lengthMismatch, user.getNativeLanguage());

        saveFeedback(attempt, user, FeedbackType.ACCURACY,
                levelOf(feedback.accuracy().rating()), feedback.accuracy().comment());
        saveFeedback(attempt, user, FeedbackType.INTONATION,
                levelOf(feedback.intonation().rating()), feedback.intonation().comment());
        saveFeedback(attempt, user, FeedbackType.LENGTH,
                levelOf(feedback.duration().rating()), feedback.duration().comment());
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

    // GPT가 주는 rating("GOOD"/"NORMAL"/"BAD")을 우리 FeedbackLevel enum으로 변환 (BAD -> WEAK, 이름이 다름)
    private FeedbackLevel levelOf(String rating) {
        return switch (rating) {
            case "GOOD" -> FeedbackLevel.GOOD;
            case "NORMAL" -> FeedbackLevel.NORMAL;
            default -> FeedbackLevel.WEAK;
        };
    }

    // 오답 기록은 회원·콘텐츠당 1건만 두고 갱신한다
    private void updateWrongAnswer(User user, LearningContent content, boolean passed) {
        wrongAnswerRepository.findByUserAndLearningContent(user, content)
                .ifPresentOrElse(
                        wrongAnswer -> wrongAnswer.recordAttempt(passed), // 더티 체킹으로 반영
                        () -> {
                            // 처음부터 맞히면 오답 기록을 만들 필요가 없다
                            if (!passed) {
                                wrongAnswerRepository.save(WrongAnswer.builder()
                                        .user(user)
                                        .learningContent(content)
                                        .wrongCount(1)
                                        .isSolved(false)
                                        .lastAttemptedAt(LocalDateTime.now())
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