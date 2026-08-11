package com.daehanforeigner.capstone.domain.learning_content.controller;

import com.daehanforeigner.capstone.domain.learning_content.dto.user.StudyContentResponseDTO;
import com.daehanforeigner.capstone.domain.learning_content.service.StudyContentService;
import com.daehanforeigner.capstone.global.rsdata.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contents")
@RequiredArgsConstructor
@Tag(name = "학습 콘텐츠", description = "회원용 학습 콘텐츠 조회")
public class StudyContentController {

    private final StudyContentService studyContentService;

    // 발음 연습 화면 진입 시 호출 — 콘텐츠 상세 조회
    @Operation(
            summary = "학습 콘텐츠 상세 조회",
            description = """
                    발음 연습 화면 진입 시 호출합니다. 화면에 필요한 값이 한 번에 내려옵니다.

                    - `text` — 학습할 단어·문장 (화면 중앙 큰 글씨)
                    - `standardPronunciationText` — 표준 발음 표기 (예: 사과 → \\[사과\\])
                    - `nativePronunciation` — 모국어 발음 표기
                    - `answerAudioUrl` / `answerVideoUrl` — 원어민 음성·영상. 그대로 재생하면 됩니다

                    **`answerAudioUrl`·`answerVideoUrl`은 null일 수 있습니다.**
                    관리자가 발음 자료를 아직 등록하지 않은 콘텐츠인 경우이며,
                    이때는 원어민 듣기·따라하기 버튼을 비활성화해 주세요.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`"),
            @ApiResponse(responseCode = "404", description = "`CONTENT_NOT_FOUND` 존재하지 않는 콘텐츠")
    })
    @GetMapping("/{contentId}")
    public ResponseEntity<RsData<StudyContentResponseDTO>> getStudyContent(
            @Parameter(description = "학습 콘텐츠 ID", example = "10")
            @PathVariable("contentId") Long contentId) {
        return ResponseEntity.ok(RsData.success(studyContentService.getStudyContent(contentId)));
    }
}
