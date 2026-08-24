package com.daehanforeigner.capstone.domain.learning_content.controller;

import com.daehanforeigner.capstone.domain.content_category.entity.CategoryType;
import com.daehanforeigner.capstone.domain.learning_content.dto.user.StudyContentListResponseDTO;
import com.daehanforeigner.capstone.domain.learning_content.dto.user.StudyContentResponseDTO;
import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.learning_content.entity.StudyStatus;
import com.daehanforeigner.capstone.domain.learning_content.service.StudyContentService;
import com.daehanforeigner.capstone.global.dto.PageResponseDTO;
import com.daehanforeigner.capstone.global.rsdata.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contents")
@RequiredArgsConstructor
@Tag(name = "학습 콘텐츠", description = "회원용 학습 콘텐츠 조회")
public class StudyContentController {

    private final StudyContentService studyContentService;

    // 공부용·문화 학습 화면 진입 시 호출 — 카드 목록 조회
    @Operation(
            summary = "학습 콘텐츠 목록 조회",
            description = """
                    공부용 학습·문화 학습의 카드 목록을 조회합니다. 모든 필터는 생략 가능하며, 생략하면 전체를 조회합니다.

                    - `categoryType` — `BASIC`(공부용 학습) / `CULTURE`(문화 학습)
                    - `status` — 회원 기준 학습 상태 필터
                      - `NOT_STARTED` 한 번도 시도하지 않음
                      - `WRONG` 아직 통과하지 못함
                      - `COMPLETED` 통과 이력 있음

                    응답의 `status`는 카드 배지에 그대로 쓰면 됩니다.
                    `answerAudioUrl`은 관리자가 발음 자료를 등록하지 않은 콘텐츠면 null이므로,
                    이때는 미리 듣기 버튼을 비활성화해 주세요.

                    정렬은 `sort` 파라미터로 지정합니다. **`contentId` · `difficulty` · `text`만 가능하며,
                    다른 값을 넣으면 400 `INVALID_SORT_PROPERTY`가 반환됩니다** (대소문자 구분)

                    무한 스크롤은 응답의 `hasNext`로 판단하세요.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "`INVALID_SORT_PROPERTY` 정렬할 수 없는 항목"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`")
    })
    @GetMapping
    public ResponseEntity<RsData<PageResponseDTO<StudyContentListResponseDTO>>> getStudyContents(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "학습 구분", example = "BASIC")
            @RequestParam(value = "categoryType", required = false) CategoryType categoryType,

            @Parameter(description = "세부 카테고리 ID", example = "1")
            @RequestParam(value = "categoryId", required = false) Long categoryId,

            @Parameter(description = "콘텐츠 유형", example = "WORD")
            @RequestParam(value = "contentType", required = false) ContentType contentType,

            @Parameter(description = "난이도", example = "BEGINNER")
            @RequestParam(value = "difficulty", required = false) Difficulty difficulty,

            @Parameter(description = "학습 상태", example = "WRONG")
            @RequestParam(value = "status", required = false) StudyStatus status,

            // @ParameterObject가 있어야 Swagger가 page·size·sort 세 개의 쿼리 파라미터로 펼쳐 보여준다
            @ParameterObject
            @PageableDefault(size = 20, sort = "contentId", direction = Sort.Direction.ASC)
            Pageable pageable) {

        return ResponseEntity.ok(RsData.success(studyContentService.getStudyContents(
                userId, categoryType, categoryId, contentType, difficulty, status, pageable)));
    }

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
