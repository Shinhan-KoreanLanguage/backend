package com.daehanforeigner.capstone.domain.admin.controller;

import com.daehanforeigner.capstone.domain.admin.dto.AdminProfileResponseDTO;
import com.daehanforeigner.capstone.domain.admin.service.AdminService;
import com.daehanforeigner.capstone.domain.content_category.dto.CategoryResponseDTO;
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
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "관리자", description = "관리자 전용 — ROLE_ADMIN 권한 필요 (일반 회원 호출 시 403)")
public class AdminController {

    private final AdminService adminService;

    @Operation(
            summary = "관리자 정보 조회",
            description = """
                    관리자 페이지 진입 시 호출합니다. 권한 확인 용도로도 사용할 수 있습니다.

                    **권한은 로그인 시점의 토큰에 담깁니다.**
                    DB에서 권한을 변경했다면 다시 로그인해야 반영됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`"),
            @ApiResponse(responseCode = "403", description = "`FORBIDDEN` 관리자 권한 없음"),
            @ApiResponse(responseCode = "404", description = "`USER_NOT_FOUND` 존재하지 않는 회원")
    })
    @GetMapping
    public ResponseEntity<RsData<AdminProfileResponseDTO>> getAdminProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(RsData.success(adminService.getAdminProfile(userId)));
    }

    @Operation(
            summary = "학습 카테고리 목록 조회",
            description = """
                    콘텐츠 등록·수정 화면의 카테고리 선택 드롭다운을 채울 때 사용합니다.
                    응답의 `categoryId`를 콘텐츠 등록 요청의 `categoryId`로 그대로 넣으면 됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`"),
            @ApiResponse(responseCode = "403", description = "`FORBIDDEN` 관리자 권한 없음")
    })
    @GetMapping("/categories")
    public ResponseEntity<RsData<List<CategoryResponseDTO>>> getCategories() {
        return ResponseEntity.ok(RsData.success(adminService.getCategories()));
    }
}
