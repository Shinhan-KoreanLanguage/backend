package com.daehanforeigner.capstone.domain.pronunciation_attempt.controller;

import com.daehanforeigner.capstone.domain.pronunciation_attempt.dto.AttemptResultResponseDTO;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.service.PronunciationAttemptService;
import com.daehanforeigner.capstone.global.rsdata.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PronunciationAttemptController {

    private final PronunciationAttemptService pronunciationAttemptService;

    // 녹음 종료 시 호출 — 녹음 파일 업로드 후 AI 분석 결과를 저장한다
    // 오디오와 영상은 AI 서버가 별도 파일을 요구하므로 각각 받는다 (영상은 웹캠 거부 시 생략 가능)
    @PostMapping("/contents/{contentId}/attempts")
    public ResponseEntity<RsData<Long>> createAttempt(
            @AuthenticationPrincipal Long userId,
            @PathVariable("contentId") Long contentId,
            @RequestPart(value = "audio", required = false) MultipartFile audio,
            @RequestPart(value = "video", required = false) MultipartFile video,
            @RequestParam(value = "durationMs", required = false) Integer durationMs) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RsData.success(
                        pronunciationAttemptService.createAttempt(userId, contentId, audio, video, durationMs)));
    }

    // 피드백 화면 진입 시 호출 — 저장된 분석 결과 조회
    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<RsData<AttemptResultResponseDTO>> getAttemptResult(
            @AuthenticationPrincipal Long userId,
            @PathVariable("attemptId") Long attemptId) {
        return ResponseEntity.ok(RsData.success(
                pronunciationAttemptService.getAttemptResult(userId, attemptId)));
    }
}