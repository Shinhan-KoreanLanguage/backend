package com.daehanforeigner.capstone.domain.open_ai.controller;

import com.daehanforeigner.capstone.domain.open_ai.dto.PronunciationFeedbackRequest;
import com.daehanforeigner.capstone.domain.open_ai.service.OpenAiService;
import com.daehanforeigner.capstone.domain.user.entity.User;
import com.daehanforeigner.capstone.domain.user.repository.UserRepository;
import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/open-ai")
@RequiredArgsConstructor
public class OpenAIController {

    private final OpenAiService openAiService;
    private final UserRepository userRepository;

    @PostMapping("/pronunciation-feedback")
    public String getPronunciationFeedback(@RequestBody PronunciationFeedbackRequest request) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        return openAiService.generatePronunciationFeedback(
                request.recognizedText(),
                request.accuracy(),
                request.lipScore(),
                request.voiceScore(),
                user.getNativeLanguage()
        );
    }
}
