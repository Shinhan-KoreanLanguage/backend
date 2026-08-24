package com.daehanforeigner.capstone.domain.phoneme_score.controller;

import com.daehanforeigner.capstone.domain.phoneme_score.dto.PhonemeAccuracyResponseDTO;
import com.daehanforeigner.capstone.domain.phoneme_score.service.PhonemeStatService;
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
@RequestMapping("/api/v1/stats/phoneme-accuracy")
@RequiredArgsConstructor
@Tag(name = "통계", description = "학습 통계 · 발음(음소) 취약점")
public class PhonemeStatController {

    private final PhonemeStatService phonemeStatService;

    // 통계 화면의 발음 취약점 분석 진입 시 호출
    @Operation(
            summary = "발음(음소)별 취약점 분석 조회",
            description = """
                    지금까지 시도한 모든 발음 기록을 음소 단위로 묶어 평균 점수·시도 횟수·취약(isWeak) 판정 횟수를 조회합니다.
                    취약 판정을 많이 받은 음소가 먼저 오고, 그다음은 평균 점수가 낮은 순으로 정렬됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`")
    })
    @GetMapping
    public ResponseEntity<RsData<List<PhonemeAccuracyResponseDTO>>> getPhonemeAccuracy(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(RsData.success(phonemeStatService.getPhonemeAccuracy(userId)));
    }
}
