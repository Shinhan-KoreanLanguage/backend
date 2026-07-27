package com.daehanforeigner.capstone.domain.open_ai.controller;

import com.daehanforeigner.capstone.domain.open_ai.dto.PronunciationFeedbackRequest;
import com.daehanforeigner.capstone.domain.open_ai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/open-ai")
@RequiredArgsConstructor
public class OpenAIController {

    private final OpenAiService openAiService;

    @PostMapping("/pronunciation-feedback")
    public String getPronunciationFeedback(@RequestBody PronunciationFeedbackRequest request) {
        return openAiService.generatePronunciationFeedback(
                request.recognizedText(),
                request.accuracy(),
                request.lipScore(),
                request.voiceScore()
        );
    }
}
