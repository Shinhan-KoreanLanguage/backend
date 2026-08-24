package com.daehanforeigner.capstone.domain.ranking.controller;

import com.daehanforeigner.capstone.domain.ranking.dto.MyRankingResponseDTO;
import com.daehanforeigner.capstone.domain.ranking.dto.RankingResponseDTO;
import com.daehanforeigner.capstone.domain.ranking.service.RankingService;
import com.daehanforeigner.capstone.global.rsdata.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/v1/rankings")
@RequiredArgsConstructor
@Tag(name = "발음 게임 랭킹", description = "TOP10 랭킹 · 개인 순위 조회")
public class RankingController {

    private final RankingService rankingService;

    @Operation(
            summary = "TOP10 랭킹 조회",
            description = "역대 최고 기록(정확히 발음한 단어 개수) 기준 상위 10명을 조회합니다. 동점이면 먼저 기록한 사람이 앞섭니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공 — 기록이 10명 미만이면 그만큼만 반환")
    })
    @GetMapping("/top10")
    public ResponseEntity<RsData<List<RankingResponseDTO>>> getTop10() {
        return ResponseEntity.ok(RsData.success(rankingService.getTop10()));
    }

    @Operation(
            summary = "개인 랭킹 조회",
            description = """
                    내 최고 기록과 현재 전체 순위를 조회합니다.
                    게임 기록이 한 번도 없으면 `hasRecord`가 `false`로 내려오니, 이때는
                    "테스트 성공 시 랭킹에 등록됩니다" 같은 안내 문구를 보여주세요.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`")
    })
    @GetMapping("/me")
    public ResponseEntity<RsData<MyRankingResponseDTO>> getMyRanking(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {

        return ResponseEntity.ok(RsData.success(rankingService.getMyRanking(userId)));
    }
}
