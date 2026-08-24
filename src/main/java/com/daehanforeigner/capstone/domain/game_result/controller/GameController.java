package com.daehanforeigner.capstone.domain.game_result.controller;

import com.daehanforeigner.capstone.domain.game_result.dto.GameFinishResponseDTO;
import com.daehanforeigner.capstone.domain.game_result.dto.GameStartResponseDTO;
import com.daehanforeigner.capstone.domain.game_result.dto.WordSubmitResponseDTO;
import com.daehanforeigner.capstone.domain.game_result.service.GameService;
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
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
@Tag(name = "발음 게임", description = "시간 제한 발음 게임 — 단어 제시 · 발음 제출 · 결과 집계")
public class GameController {

    private final GameService gameService;

    @Operation(
            summary = "게임 시작",
            description = """
                    게임 카테고리를 선택하고 "시작하기"를 누르면 호출합니다.
                    제시할 단어 목록을 한 번에 셔플해서 내려주므로, 프론트에서 1분 타이머와 함께
                    순서대로 하나씩 보여주면 됩니다.

                    응답의 `gameResultId`를 이후 단어 제출·게임 종료 API에서 계속 사용하세요.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "게임 시작 성공"),
            @ApiResponse(responseCode = "404",
                    description = "`CATEGORY_NOT_FOUND` 존재하지 않는 카테고리 / `NO_WORDS_AVAILABLE` 해당 카테고리에 단어 콘텐츠 없음")
    })
    @PostMapping("/start")
    public ResponseEntity<RsData<GameStartResponseDTO>> startGame(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "게임 카테고리 ID", example = "3")
            @RequestParam("categoryId") Long categoryId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RsData.success(gameService.startGame(userId, categoryId)));
    }

    @Operation(
            summary = "단어 발음 제출",
            description = """
                    제시된 단어 하나를 발음하고 녹음이 끝나면 호출합니다. **`multipart/form-data`로 보내야 합니다.**

                    영상은 받지 않습니다 — 게임은 속도가 중요해서 입모양 분석 없이 음성만으로 판정합니다.
                    응답의 `isCorrect`로 즉시 O/X 피드백을 보여주고 다음 단어로 넘어가면 됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "판정 완료"),
            @ApiResponse(responseCode = "400", description = "`EMPTY_FILE` 오디오 누락"),
            @ApiResponse(responseCode = "404",
                    description = "`GAME_NOT_FOUND` 존재하지 않거나 본인 게임이 아님 / `CONTENT_NOT_FOUND` 존재하지 않는 단어"),
            @ApiResponse(responseCode = "409", description = "`GAME_ALREADY_FINISHED` 이미 종료된 게임"),
            @ApiResponse(responseCode = "503", description = "`AI_SERVER_ERROR` 분석 서버 통신 실패")
    })
    @PostMapping(value = "/{gameResultId}/words", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RsData<WordSubmitResponseDTO>> submitWord(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "게임 시작 응답으로 받은 gameResultId", example = "7")
            @PathVariable("gameResultId") Long gameResultId,
            @Parameter(description = "제시받은 단어의 wordId (GameStartResponseDTO.words[].wordId)", example = "10")
            @RequestParam("wordId") Long wordId,
            @Parameter(description = "녹음 음성 파일 (webm · mp3 · wav · m4a)")
            @RequestPart("audio") MultipartFile audio) {

        return ResponseEntity.ok(RsData.success(
                gameService.submitWord(userId, gameResultId, wordId, audio)));
    }

    @Operation(
            summary = "게임 종료",
            description = """
                    1분 제한 시간이 끝나면 호출합니다. 그때까지 정확히 발음한 단어 개수를 최종 점수로
                    집계하고, 역대 최고 기록이면 랭킹에 반영합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "집계 완료"),
            @ApiResponse(responseCode = "404", description = "`GAME_NOT_FOUND` 존재하지 않거나 본인 게임이 아님"),
            @ApiResponse(responseCode = "409", description = "`GAME_ALREADY_FINISHED` 이미 종료된 게임")
    })
    @PostMapping("/{gameResultId}/finish")
    public ResponseEntity<RsData<GameFinishResponseDTO>> finishGame(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "게임 시작 응답으로 받은 gameResultId", example = "7")
            @PathVariable("gameResultId") Long gameResultId) {

        return ResponseEntity.ok(RsData.success(gameService.finishGame(userId, gameResultId)));
    }
}
