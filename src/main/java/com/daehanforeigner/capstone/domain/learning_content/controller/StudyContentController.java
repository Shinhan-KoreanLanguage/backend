package com.daehanforeigner.capstone.domain.learning_content.controller;

import com.daehanforeigner.capstone.domain.learning_content.dto.user.StudyContentResponseDTO;
import com.daehanforeigner.capstone.domain.learning_content.service.StudyContentService;
import com.daehanforeigner.capstone.global.rsdata.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contents")
@RequiredArgsConstructor
public class StudyContentController {

    private final StudyContentService studyContentService;

    // 발음 연습 화면 진입 시 호출 — 콘텐츠 상세 조회
    @GetMapping("/{contentId}")
    public ResponseEntity<RsData<StudyContentResponseDTO>> getStudyContent(
            @PathVariable("contentId") Long contentId) {
        return ResponseEntity.ok(RsData.success(studyContentService.getStudyContent(contentId)));
    }
}