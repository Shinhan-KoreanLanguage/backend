package com.daehanforeigner.capstone.domain.admin.controller;

import com.daehanforeigner.capstone.domain.admin.service.AdminLearningContentService;
import com.daehanforeigner.capstone.domain.learning_content.dto.admin.LearningContentRequestDTO;
import com.daehanforeigner.capstone.domain.learning_content.dto.admin.LearningContentResponseDTO;
import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.global.dto.PageResponseDTO;
import com.daehanforeigner.capstone.global.rsdata.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/contents")
@RequiredArgsConstructor
public class AdminLearningContentController {

    private final AdminLearningContentService adminLearningContentService;

    // 목록 조회 ex) ?categoryId=1&contentType=WORD&difficulty=BEGINNER&keyword=사과&page=0&size=20
    @GetMapping
    public ResponseEntity<RsData<PageResponseDTO<LearningContentResponseDTO>>> getContents(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "contentType", required = false) ContentType contentType,
            @RequestParam(value = "difficulty", required = false) Difficulty difficulty,
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault(size = 20, sort = "contentId", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(RsData.success(
                adminLearningContentService.getContents(categoryId, contentType, difficulty, keyword, pageable)));
    }

    // 등록 (multipart/form-data)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RsData<Long>> createContent(
            @Valid @RequestPart("content") LearningContentRequestDTO request,
            @RequestPart(value = "audioFile", required = false) MultipartFile audioFile,
            @RequestPart(value = "videoFile", required = false) MultipartFile videoFile) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RsData.success(adminLearningContentService.createContent(request, audioFile, videoFile)));
    }

    // 수정 (multipart/form-data) — 파일을 안 보내면 기존 파일 유지
    @PutMapping(value = "/{contentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RsData<String>> updateContent(
            @PathVariable("contentId") Long contentId,
            @Valid @RequestPart("content") LearningContentRequestDTO request,
            @RequestPart(value = "audioFile", required = false) MultipartFile audioFile,
            @RequestPart(value = "videoFile", required = false) MultipartFile videoFile) {

        adminLearningContentService.updateContent(contentId, request, audioFile, videoFile);
        return ResponseEntity.ok(RsData.success("학습 콘텐츠가 수정되었습니다."));
    }

    // 단일 삭제
    @DeleteMapping("/{contentId}")
    public ResponseEntity<RsData<String>> deleteContent(@PathVariable("contentId") Long contentId) {
        adminLearningContentService.deleteContent(contentId);
        return ResponseEntity.ok(RsData.success("학습 콘텐츠가 삭제되었습니다."));
    }

    // 일괄 삭제 (DELETE /api/v1/admin/contents?ids=1,2,3)
    @DeleteMapping
    public ResponseEntity<RsData<String>> deleteContents(@RequestParam(value = "ids", required = false) List<Long> ids) { // 파라미터가 없을 경우 null 처리 됨에 따라 400 에러 발생
        adminLearningContentService.deleteContents(ids);
        return ResponseEntity.ok(RsData.success("선택한 학습 콘텐츠가 삭제되었습니다."));
    }
}