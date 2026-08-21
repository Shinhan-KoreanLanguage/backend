package com.daehanforeigner.capstone.domain.wrong_answer.controller;

import com.daehanforeigner.capstone.domain.learning_content.entity.Difficulty;
import com.daehanforeigner.capstone.domain.wrong_answer.dto.WrongAnswerResponseDTO;
import com.daehanforeigner.capstone.domain.wrong_answer.dto.WrongAnswerSummaryResponseDTO;
import com.daehanforeigner.capstone.domain.wrong_answer.entity.WrongAnswerTab;
import com.daehanforeigner.capstone.domain.wrong_answer.service.WrongAnswerService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wrong-answers")
@RequiredArgsConstructor
@Tag(name = "오답 정리", description = "오답 노트 조회 및 정리")
public class WrongAnswerController {

    private final WrongAnswerService wrongAnswerService;

    // 오답 노트 목록 — 탭·필터·검색·정렬
    @Operation(
            summary = "오답 목록 조회",
            description = """
                    발음 평가에서 통과하지 못한 콘텐츠 목록을 조회합니다. **모든 필터는 선택입니다.**

                    - `tab` — 화면 상단 탭. `ALL`(전체) / `WORD`(단어, 음절 포함) / `SENTENCE`(문장)
                    - `solved` — `false`면 아직 못 맞힌 것만, `true`면 복습 완료한 것만
                    - `keyword` — 학습 텍스트와 표준 발음 표기를 부분 일치로 검색합니다
                    - 정렬은 `sort` 파라미터로 지정합니다
                      - 가장 많이 틀린 순 → `wrongCount,desc`
                      - 최근 학습 순 → `lastAttemptedAt,desc` (기본값)
                      - 정확도 낮은 순 → `lastAccuracy,asc`

                    다시 연습하려면 응답의 `contentId`로 발음 연습 화면(`GET /api/v1/contents/{contentId}`)에
                    진입하면 됩니다. 별도의 재도전 API는 없습니다.

                    재시도해서 통과하면 `solved`가 `true`로 바뀌며, 목록에서 자동으로 사라지지는 않습니다.
                    통과한 항목을 감추려면 `solved=false`로 조회하세요.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`")
    })
    @GetMapping
    public ResponseEntity<RsData<PageResponseDTO<WrongAnswerResponseDTO>>> getWrongAnswers(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "탭 (선택) — 생략하면 전체", example = "WORD")
            @RequestParam(value = "tab", required = false) WrongAnswerTab tab,

            @Parameter(description = "복습 완료 여부 (선택)", example = "false")
            @RequestParam(value = "solved", required = false) Boolean solved,

            @Parameter(description = "카테고리 ID (선택)", example = "1")
            @RequestParam(value = "categoryId", required = false) Long categoryId,

            @Parameter(description = "난이도 (선택)", example = "BEGINNER")
            @RequestParam(value = "difficulty", required = false) Difficulty difficulty,

            @Parameter(description = "검색어 (선택) — 학습 텍스트·표준 발음 표기 부분 일치", example = "사과")
            @RequestParam(value = "keyword", required = false) String keyword,

            @ParameterObject
            @PageableDefault(size = 20, sort = "lastAttemptedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(RsData.success(wrongAnswerService.getWrongAnswers(
                userId, tab, solved, categoryId, difficulty, keyword, pageable)));
    }

    // 오답 노트 우측 요약 패널
    @Operation(
            summary = "오답 요약 및 정확도 분포 조회",
            description = """
                    오답 노트 우측의 요약 카드와 정확도 분포 도넛에 필요한 값을 한 번에 조회합니다.
                    상단 탭의 개수(전체 · 단어 · 문장)도 이 응답을 그대로 쓰면 됩니다.

                    정확도 분포는 **개수**로 내려갑니다. 화면에 비율로 표시하려면 `totalCount`로 나눠 주세요.

                    아직 재시도 기록이 없어 정확도가 없는 오답은 어느 구간에도 포함되지 않으므로,
                    네 구간의 합이 `totalCount`보다 작을 수 있습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`")
    })
    @GetMapping("/summary")
    public ResponseEntity<RsData<WrongAnswerSummaryResponseDTO>> getSummary(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(RsData.success(wrongAnswerService.getSummary(userId)));
    }

    // 오답 목록에서 제외
    @Operation(
            summary = "오답 삭제",
            description = """
                    오답 목록에서 해당 항목을 제거합니다.

                    **학습 기록 자체는 지워지지 않습니다.** 오답 표시만 사라지므로 통계와 학습 현황에는 영향이 없습니다.
                    삭제하면 학습 콘텐츠 목록의 배지도 `WRONG`에서 `NOT_STARTED`로 바뀝니다.

                    다른 회원의 오답을 삭제하려 하면 404가 반환됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`"),
            @ApiResponse(responseCode = "404", description = "`WRONG_ANSWER_NOT_FOUND` 없거나 본인 것이 아님")
    })
    @DeleteMapping("/{wrongId}")
    public ResponseEntity<RsData<String>> deleteWrongAnswer(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "삭제할 오답 기록 ID", example = "3")
            @PathVariable("wrongId") Long wrongId) {

        wrongAnswerService.deleteWrongAnswer(userId, wrongId);

        return ResponseEntity.ok(RsData.success("오답 기록이 삭제되었습니다."));
    }
}
