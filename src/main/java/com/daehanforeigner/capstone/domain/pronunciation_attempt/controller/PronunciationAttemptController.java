package com.daehanforeigner.capstone.domain.pronunciation_attempt.controller;

import com.daehanforeigner.capstone.domain.pronunciation_attempt.dto.AttemptResultResponseDTO;
import com.daehanforeigner.capstone.domain.pronunciation_attempt.service.PronunciationAttemptService;
import com.daehanforeigner.capstone.global.rsdata.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "발음 평가", description = "녹음 업로드 · AI 분석 결과 조회")
public class PronunciationAttemptController {

    private final PronunciationAttemptService pronunciationAttemptService;

    // 녹음 종료 시 호출 — 녹음 파일 업로드 후 AI 분석 결과를 저장한다
    // 오디오와 영상은 AI 서버가 별도 파일을 요구하므로 각각 받는다 (영상은 웹캠 거부 시 생략 가능)
    @Operation(
            summary = "발음 녹음 업로드 및 분석",
            description = """
                    녹음 종료 시 호출합니다. **`multipart/form-data`로 보내야 합니다.** (JSON 아님)

                    | 파트 | 필수 | 설명 |
                    |---|---|---|
                    | `audio` | 필수 | 녹음 음성. webm · mp3 · wav · m4a (MediaRecorder 기본 출력이 webm) |
                    | `video` | 선택 | 웹캠 녹화. mp4 · webm · avi · mov |
                    | `durationMs` | 선택 | 녹음 길이(밀리초). 쿼리 파라미터로 보냅니다 |

                    **영상을 보내지 않으면 입모양 점수(`lipScore`)가 null로 저장됩니다.**
                    사용자가 웹캠을 거부한 경우 audio만 보내면 됩니다.

                    ### 응답과 소요 시간
                    응답의 `data`는 **attemptId(숫자)** 입니다.
                    이 값으로 `GET /attempts/{attemptId}`를 호출해 상세 결과를 받아가세요.

                    AI 분석이 포함되어 **수 초에서 수십 초까지 걸릴 수 있습니다.**
                    프론트 요청 타임아웃을 넉넉히(60초 이상) 잡고 로딩 화면을 띄워주세요.

                    ### 정답 판정
                    종합 점수(`accuracy`) 85% 이상이면 통과입니다.
                    미달이면 오답 목록에 쌓이고, 재시도해서 통과하면 해결 처리됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "분석 완료 — data에 attemptId"),
            @ApiResponse(responseCode = "400",
                    description = "`EMPTY_FILE` 파일 누락 / `INVALID_FILE_TYPE` 허용되지 않는 확장자 / "
                            + "`AI_MEDIA_ANALYSIS_FAILED` 얼굴 미검출 등으로 분석 불가"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`"),
            @ApiResponse(responseCode = "404",
                    description = "`CONTENT_NOT_FOUND` 존재하지 않는 콘텐츠 / "
                            + "`AI_REFERENCE_NOT_FOUND` 원어민 기준이 등록되지 않은 콘텐츠"),
            @ApiResponse(responseCode = "503", description = "`AI_SERVER_ERROR` 분석 서버 통신 실패 — 재시도 안내 필요")
    })
    @PostMapping(value = "/contents/{contentId}/attempts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RsData<Long>> createAttempt(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "학습 콘텐츠 ID", example = "10")
            @PathVariable("contentId") Long contentId,
            @Parameter(description = "녹음 음성 파일 (webm · mp3 · wav · m4a)")
            @RequestPart(value = "audio", required = false) MultipartFile audio,
            @Parameter(description = "웹캠 녹화 영상 (선택) — 없으면 입모양 점수가 null")
            @RequestPart(value = "video", required = false) MultipartFile video,
            @Parameter(description = "녹음 길이(밀리초)", example = "2400")
            @RequestParam(value = "durationMs", required = false) Integer durationMs) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RsData.success(
                        pronunciationAttemptService.createAttempt(userId, contentId, audio, video, durationMs)));
    }

    // 피드백 화면 진입 시 호출 — 저장된 분석 결과 조회
    @Operation(
            summary = "발음 분석 결과 조회",
            description = """
                    피드백 화면 진입 시 호출합니다. 화면 구성에 필요한 값이 한 번에 내려옵니다.

                    - 화면 상단 큰 숫자 → `accuracy`
                    - 점수 카드 3개 → `voiceScore`(발음) · `lipScore`(입모양) · `pitchScore`(억양)
                    - 파형 음절 분할 → `syllableScores`의 `startTime`~`endTime`
                    - 억양 그래프 → `userPitchData` vs `nativePitchData`
                    - 상세 피드백 카드 → `feedbacks`

                    ### 주의
                    - `userPitchData` · `nativePitchData` · `pitchHighlightSegments`는
                      **JSON 문자열**이라 `JSON.parse()` 후 사용해야 합니다
                    - `lipScore`는 영상을 보내지 않았으면 null입니다
                    - 본인의 시도만 조회할 수 있습니다
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`"),
            @ApiResponse(responseCode = "403", description = "`FORBIDDEN` 다른 회원의 시도 조회"),
            @ApiResponse(responseCode = "404", description = "`ATTEMPT_NOT_FOUND` 존재하지 않는 시도")
    })
    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<RsData<AttemptResultResponseDTO>> getAttemptResult(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "녹음 업로드 응답으로 받은 attemptId", example = "42")
            @PathVariable("attemptId") Long attemptId) {
        return ResponseEntity.ok(RsData.success(
                pronunciationAttemptService.getAttemptResult(userId, attemptId)));
    }
}
