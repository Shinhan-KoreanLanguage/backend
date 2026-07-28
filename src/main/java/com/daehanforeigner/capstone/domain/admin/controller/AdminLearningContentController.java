package com.daehanforeigner.capstone.domain.admin.controller;

import com.daehanforeigner.capstone.domain.admin.service.AdminLearningContentService;
import com.daehanforeigner.capstone.domain.learning_content.dto.LearningContentRequestDTO;
import com.daehanforeigner.capstone.domain.learning_content.dto.LearningContentResponseDTO;
import com.daehanforeigner.capstone.global.rsdata.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/contents")
@RequiredArgsConstructor
public class AdminLearningContentController {

    private final AdminLearningContentService adminLearningContentService;

    // 전체 목록 조회
    @GetMapping
    public ResponseEntity<RsData<List<LearningContentResponseDTO>>> getContents() {
        return ResponseEntity.ok(RsData.success(adminLearningContentService.getContents()));
    }

    // 등록
    @PostMapping
    public ResponseEntity<RsData<Long>> createContent(
            @Valid @RequestBody LearningContentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RsData.success(adminLearningContentService.createContent(request)));
    }

    // 수정 (전체 교체)
    @PutMapping("/{contentId}")
    public ResponseEntity<RsData<String>> updateContent(
            @PathVariable("contentId") Long contentId,
            @Valid @RequestBody LearningContentRequestDTO request) {
        adminLearningContentService.updateContent(contentId, request);
        return ResponseEntity.ok(RsData.success("학습 콘텐츠가 수정되었습니다."));
    }

    // 삭제
    @DeleteMapping("/{contentId}")
    public ResponseEntity<RsData<String>> deleteContent(@PathVariable("contentId") Long contentId) {
        adminLearningContentService.deleteContent(contentId);
        return ResponseEntity.ok(RsData.success("학습 콘텐츠가 삭제되었습니다."));
    }
}