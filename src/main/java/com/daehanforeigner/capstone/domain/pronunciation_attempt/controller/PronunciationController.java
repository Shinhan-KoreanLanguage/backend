package com.daehanforeigner.capstone.domain.pronunciation_attempt.controller;

import com.daehanforeigner.capstone.domain.open_ai.service.OpenAiService;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.client.FastApiClient;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.dto.PronunciationAnalysisResponse;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.dto.PronunciationResultResponse;
import com.daehanforeigner.capstone.domain.user.entity.User;
import com.daehanforeigner.capstone.domain.user.repository.UserRepository;
import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pronunciation")
@RequiredArgsConstructor
public class PronunciationController {

    private final FastApiClient fastApiClient;
    private final OpenAiService openAiService;
    private final UserRepository userRepository;

    @PostMapping(value = "/analyze", consumes = "multipart/form-data")
    public PronunciationResultResponse analyze(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam("targetText") String targetText,
            @RequestParam(value = "video", required = false) MultipartFile video,
            @RequestParam(value = "referenceVideo", required = false) MultipartFile referenceVideo
    ) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        PronunciationAnalysisResponse analysis = fastApiClient.analyze(audio, targetText, video, referenceVideo);

        int mouthScore = analysis.mouthAccuracy() != null ? analysis.mouthAccuracy().intValue() : 0;
        int voiceScore = (int) analysis.sttAccuracy();

        String feedback = openAiService.generatePronunciationFeedback(
                analysis.recognizedText(),
                analysis.finalAccuracy(),
                mouthScore,
                voiceScore,
                user.getNativeLanguage()
        );

        return new PronunciationResultResponse(
                analysis.recognizedText(),
                analysis.finalAccuracy(),
                analysis.isCorrect(),
                feedback
        );
    }
}
