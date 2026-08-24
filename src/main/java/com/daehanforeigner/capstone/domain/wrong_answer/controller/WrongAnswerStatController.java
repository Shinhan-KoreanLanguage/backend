package com.daehanforeigner.capstone.domain.wrong_answer.controller;

import com.daehanforeigner.capstone.domain.wrong_answer.dto.WrongAnswerStatResponseDTO;
import com.daehanforeigner.capstone.domain.wrong_answer.service.WrongAnswerStatService;
import com.daehanforeigner.capstone.global.rsdata.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stats/wrong-answers")
@RequiredArgsConstructor
@Tag(name = "통계", description = "학습 통계 · 오답")
public class WrongAnswerStatController {

    private final WrongAnswerStatService wrongAnswerStatService;

    // 오답 통계 화면 진입 시 호출
    @Operation(
            summary = "틀린 단어·문장 통계 조회",
            description = """
                    아직 재학습으로 통과하지 못한(`isSolved = false`) 오답을 틀린 횟수가 많은 순으로 조회합니다.
                    재학습에 성공해 통과 처리된 오답은 목록에서 제외됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`")
    })
    @GetMapping
    public ResponseEntity<RsData<List<WrongAnswerStatResponseDTO>>> getWrongAnswerStats(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(RsData.success(wrongAnswerStatService.getWrongAnswerStats(userId)));
    }
}
