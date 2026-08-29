package com.daehanforeigner.capstone.domain.admin.controller;

import com.daehanforeigner.capstone.domain.admin.service.AdminLearningContentService;
import com.daehanforeigner.capstone.domain.learning_content.dto.admin.LearningContentRequestDTO;
import com.daehanforeigner.capstone.domain.learning_content.dto.admin.LearningContentResponseDTO;
import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.global.dto.PageResponseDTO;
import com.daehanforeigner.capstone.global.rsdata.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "관리자 - 학습 콘텐츠", description = "학습 콘텐츠 CRUD — ROLE_ADMIN 권한 필요")
public class AdminLearningContentController {

    private final AdminLearningContentService adminLearningContentService;

    // 목록 조회 ex) ?categoryId=1&contentType=WORD&difficulty=BEGINNER&keyword=사과&page=0&size=20
    @Operation(
            summary = "학습 콘텐츠 목록 조회",
            description = """
                    필터·검색·페이징을 지원합니다. **모든 필터는 선택이며, 생략하면 조건 없이 조회됩니다.**

                    - `keyword`는 학습 텍스트와 발음 가이드를 부분 일치로 검색합니다
                    - 페이지 번호(`page`)는 **0부터** 시작하고, `size`는 최대 100까지 허용됩니다
                    - 기본 정렬은 최신 등록순(contentId 내림차순)입니다

                    - 정렬은 `contentId` · `difficulty` · `text` · `contentType`만 가능하며,
                      다른 값을 넣으면 400 `INVALID_SORT_PROPERTY`가 반환됩니다 (대소문자 구분)

                    응답의 `content` 배열이 목록이고, 페이지 정보는 그 바깥에 있습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "`INVALID_SORT_PROPERTY` 정렬할 수 없는 항목"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`"),
            @ApiResponse(responseCode = "403", description = "`FORBIDDEN` 관리자 권한 없음")
    })
    @GetMapping
    public ResponseEntity<RsData<PageResponseDTO<LearningContentResponseDTO>>> getContents(
            @Parameter(description = "카테고리 ID (선택)", example = "1")
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @Parameter(description = "콘텐츠 유형 (선택)", example = "WORD")
            @RequestParam(value = "contentType", required = false) ContentType contentType,
            @Parameter(description = "난이도 (선택)", example = "BEGINNER")
            @RequestParam(value = "difficulty", required = false) Difficulty difficulty,
            @Parameter(description = "검색어 (선택) — 학습 텍스트·발음 가이드 부분 일치", example = "사과")
            @RequestParam(value = "keyword", required = false) String keyword,
            // @ParameterObject가 있어야 Swagger가 page·size·sort 세 개의 쿼리 파라미터로 펼쳐 보여준다
            @ParameterObject
            @PageableDefault(size = 20, sort = "contentId", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(RsData.success(
                adminLearningContentService.getContents(categoryId, contentType, difficulty, keyword, pageable)));
    }

    // 등록 (multipart/form-data)
    @Operation(
            summary = "학습 콘텐츠 등록",
            description = """
                    **`multipart/form-data`로 보내야 합니다.** (JSON 아님)

                    | 파트 | 필수 | 설명 |
                    |---|---|---|
                    | `content` | 필수 | 콘텐츠 정보 **JSON**. 이 파트의 Content-Type을 `application/json`으로 지정해야 합니다 |
                    | `audioFile` | 필수 | 원어민 음성. mp3 · wav · m4a |
                    | `videoFile` | 필수 | 원어민 영상. mp4 · webm · avi · mov |

                    > **자주 겪는 실수:** `content` 파트의 Content-Type을 지정하지 않으면 500이 납니다.
                    > Postman에서는 해당 행의 Content-Type 칸에 `application/json`을 직접 입력하세요.

                    음성·영상은 **둘 다 필수**입니다. 하나라도 빠지면 `MEDIA_FILE_REQUIRED`가 반환됩니다.
                    등록 시 AI 분석 서버에 원어민 기준도 함께 등록되므로 응답이 다소 걸릴 수 있습니다.

                    ### 언어별 번역 (`translations`)
                    뜻·발음 도움말·모국어 발음 표기는 **언어마다 따로 등록**합니다.
                    회원은 자기 모국어에 해당하는 번역만 보게 되므로, 서비스 대상 언어(KR·EN·JP·CN)를
                    모두 채워주는 것이 좋습니다. 등록하지 않은 언어의 회원에게는 영어로 대체됩니다.

                    ```json
                    "translations": [
                      { "language": "EN", "meaning": "apple", "pronunciationGuide": "...", "nativePronunciation": "sa-gwa" },
                      { "language": "JP", "meaning": "りんご", "pronunciationGuide": "...", "nativePronunciation": "サグァ" }
                    ]
                    ```

                    같은 언어를 두 번 넣으면 `DUPLICATE_TRANSLATION_LANGUAGE`가 반환됩니다.

                    ### 난이도 (`difficulty`)
                    난이도 구분이 없는 콘텐츠(실생활 문장 등)는 **생략하거나 null로 보내면 됩니다.**

                    응답의 `data`는 생성된 콘텐츠 ID입니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공 — data에 콘텐츠 ID"),
            @ApiResponse(responseCode = "400",
                    description = "`MEDIA_FILE_REQUIRED` 음성·영상 누락 / `INVALID_FILE_TYPE` 허용되지 않는 확장자 / "
                            + "`DUPLICATE_TRANSLATION_LANGUAGE` 같은 언어 번역 중복 / 입력값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`"),
            @ApiResponse(responseCode = "403", description = "`FORBIDDEN` 관리자 권한 없음"),
            @ApiResponse(responseCode = "404", description = "`CATEGORY_NOT_FOUND` 존재하지 않는 카테고리"),
            @ApiResponse(responseCode = "503", description = "`AI_SERVER_ERROR` 원어민 기준 등록 실패")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RsData<Long>> createContent(
            @Parameter(description = "콘텐츠 정보 JSON — Content-Type을 application/json으로 지정")
            @Valid @RequestPart("content") LearningContentRequestDTO request,
            @Parameter(description = "원어민 음성 파일 (필수)")
            @RequestPart(value = "audioFile", required = false) MultipartFile audioFile,
            @Parameter(description = "원어민 영상 파일 (필수)")
            @RequestPart(value = "videoFile", required = false) MultipartFile videoFile) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RsData.success(adminLearningContentService.createContent(request, audioFile, videoFile)));
    }

    // 수정 (multipart/form-data) — 파일을 안 보내면 기존 파일 유지
    @Operation(
            summary = "학습 콘텐츠 수정",
            description = """
                    **`multipart/form-data`로 보내야 합니다.** (JSON 아님)

                    - `content` 파트는 **전체 값을 모두 담아 보내야 합니다.** 보내지 않은 필드는 null로 덮어써집니다
                      (부분 수정이 아닌 전체 교체 방식)
                    - **`translations`도 전체 교체입니다.** 기존 번역을 모두 지우고 보낸 것으로 다시 채우므로,
                      한 언어만 고치더라도 나머지 언어를 함께 보내야 합니다
                    - `audioFile` · `videoFile`은 **선택**입니다. 보내지 않으면 기존 파일이 유지되고,
                      보낸 파일만 교체됩니다

                    등록과 마찬가지로 `content` 파트의 Content-Type을 `application/json`으로 지정해야 합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "`INVALID_FILE_TYPE` 허용되지 않는 확장자 / 입력값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`"),
            @ApiResponse(responseCode = "403", description = "`FORBIDDEN` 관리자 권한 없음"),
            @ApiResponse(responseCode = "404",
                    description = "`CONTENT_NOT_FOUND` 존재하지 않는 콘텐츠 / `CATEGORY_NOT_FOUND` 존재하지 않는 카테고리")
    })
    @PutMapping(value = "/{contentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RsData<String>> updateContent(
            @Parameter(description = "수정할 콘텐츠 ID", example = "10")
            @PathVariable("contentId") Long contentId,
            @Parameter(description = "콘텐츠 정보 JSON (전체 값) — Content-Type을 application/json으로 지정")
            @Valid @RequestPart("content") LearningContentRequestDTO request,
            @Parameter(description = "교체할 음성 파일 (선택) — 없으면 기존 유지")
            @RequestPart(value = "audioFile", required = false) MultipartFile audioFile,
            @Parameter(description = "교체할 영상 파일 (선택) — 없으면 기존 유지")
            @RequestPart(value = "videoFile", required = false) MultipartFile videoFile) {

        adminLearningContentService.updateContent(contentId, request, audioFile, videoFile);
        return ResponseEntity.ok(RsData.success("학습 콘텐츠가 수정되었습니다."));
    }

    // 단일 삭제
    @Operation(
            summary = "학습 콘텐츠 단일 삭제",
            description = "콘텐츠와 연결된 원어민 음성·영상 자료도 함께 삭제됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`"),
            @ApiResponse(responseCode = "403", description = "`FORBIDDEN` 관리자 권한 없음"),
            @ApiResponse(responseCode = "404", description = "`CONTENT_NOT_FOUND` 존재하지 않는 콘텐츠")
    })
    @DeleteMapping("/{contentId}")
    public ResponseEntity<RsData<String>> deleteContent(
            @Parameter(description = "삭제할 콘텐츠 ID", example = "10")
            @PathVariable("contentId") Long contentId) {
        adminLearningContentService.deleteContent(contentId);
        return ResponseEntity.ok(RsData.success("학습 콘텐츠가 삭제되었습니다."));
    }

    // 일괄 삭제 (DELETE /api/v1/admin/contents?ids=1,2,3)
    @Operation(
            summary = "학습 콘텐츠 일괄 삭제",
            description = """
                    목록에서 체크박스로 선택한 콘텐츠를 한 번에 삭제합니다.
                    쿼리 파라미터로 보냅니다: `DELETE /api/v1/admin/contents?ids=1,2,3`

                    이미 삭제된 ID가 섞여 있어도 오류 없이 처리됩니다 (없는 것은 무시).
                    다만 `ids`를 아예 보내지 않으면 `CONTENT_IDS_REQUIRED`가 반환됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "`CONTENT_IDS_REQUIRED` 삭제할 콘텐츠를 선택하지 않음"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`"),
            @ApiResponse(responseCode = "403", description = "`FORBIDDEN` 관리자 권한 없음")
    })
    @DeleteMapping
    public ResponseEntity<RsData<String>> deleteContents(
            @Parameter(description = "삭제할 콘텐츠 ID 목록 (쉼표 구분)", example = "1,2,3")
            @RequestParam(value = "ids", required = false) List<Long> ids) { // 파라미터가 없을 경우 null 처리 됨에 따라 400 에러 발생
        adminLearningContentService.deleteContents(ids);
        return ResponseEntity.ok(RsData.success("선택한 학습 콘텐츠가 삭제되었습니다."));
    }
}
