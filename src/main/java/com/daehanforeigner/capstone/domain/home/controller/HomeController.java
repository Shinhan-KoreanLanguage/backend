package com.daehanforeigner.capstone.domain.home.controller;

import com.daehanforeigner.capstone.domain.home.dto.HomeSummaryResponseDTO;
import com.daehanforeigner.capstone.domain.home.service.HomeService;
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
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
@Tag(name = "홈", description = "메인 화면 학습 현황")
public class HomeController {

    private final HomeService homeService;

    // 홈 화면 진입 시 호출 — 학습 현황 카드와 발음 정확도
    @Operation(
            summary = "학습 현황 요약 조회",
            description = """
                    홈 화면 상단의 학습 현황 4종과 발음 정확도를 한 번에 조회합니다.

                    - `learnedWordCount` · `practicedSentenceCount` — **서로 다른 콘텐츠 수**입니다.
                      같은 단어를 여러 번 연습해도 1로 셉니다
                    - `studyDayCount` — 한 번이라도 연습한 날의 수
                    - `streakDayCount` — 마지막 학습일부터 하루도 빠지지 않고 이어진 일수
                    - `weeklyAccuracy` — 이번 주(월요일 기준) 발음 정확도 평균
                    - `accuracyDiff` — 지난주 대비 증감. 양수면 상승, 음수면 하락입니다

                    학습 기록이 없는 신규 회원은 모든 값이 0으로 내려옵니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`")
    })
    @GetMapping("/summary")
    public ResponseEntity<RsData<HomeSummaryResponseDTO>> getSummary(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(RsData.success(homeService.getSummary(userId)));
    }
}
