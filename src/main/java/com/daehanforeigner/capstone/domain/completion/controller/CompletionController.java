package com.daehanforeigner.capstone.domain.completion.controller;

import com.daehanforeigner.capstone.domain.completion.dto.CompletionResponseDTO;
import com.daehanforeigner.capstone.domain.completion.service.CompletionService;
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

@RestController
@RequestMapping("/api/v1/stats/completion")
@RequiredArgsConstructor
@Tag(name = "통계", description = "학습 통계 · 완료율")
public class CompletionController {

    private final CompletionService completionService;

    // 통계 화면 진입 시 호출
    @Operation(
            summary = "학습 완료율 조회",
            description = """
                    전체 학습 콘텐츠 중 합격한(서로 다른 콘텐츠 기준) 비율을 조회합니다.
                    같은 콘텐츠를 여러 번 합격해도 1로 카운트하며, 카테고리별 완료율도 함께 내려줍니다.

                    학습 콘텐츠가 아직 없는 카테고리는 완료율 0%로 응답합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`")
    })
    @GetMapping
    public ResponseEntity<RsData<CompletionResponseDTO>> getCompletion(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(RsData.success(completionService.getCompletion(userId)));
    }
}
