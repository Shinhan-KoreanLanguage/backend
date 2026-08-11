package com.daehanforeigner.capstone.domain.user.controller.profile;

import com.daehanforeigner.capstone.domain.user.dto.profile.PasswordChangeRequestDTO;
import com.daehanforeigner.capstone.domain.user.dto.profile.UserProfileResponseDTO;
import com.daehanforeigner.capstone.domain.user.dto.profile.UserUpdateRequestDTO;
import com.daehanforeigner.capstone.domain.user.service.profile.UserProfileService;
import com.daehanforeigner.capstone.global.rsdata.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "회원 정보", description = "마이페이지 — 프로필 조회 · 수정 · 비밀번호 변경")
public class UserProfileController {

    private final UserProfileService userProfileService;

    // 내 정보 조회
    @Operation(
            summary = "내 정보 조회",
            description = """
                    마이페이지 진입 시 호출합니다.
                    프로필 이미지를 설정하지 않은 회원은 `profileImageUrl`에 기본 이미지 URL이 담겨 오므로,
                    프론트에서 별도 분기 없이 그대로 사용하면 됩니다. (null이 오지 않습니다)
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`"),
            @ApiResponse(responseCode = "404", description = "`USER_NOT_FOUND` 존재하지 않는 회원")
    })
    @GetMapping("/me")
    public ResponseEntity<RsData<UserProfileResponseDTO>> getMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(RsData.success(userProfileService.getMyProfile(userId)));
    }

    // 프로필 텍스트 수정 (닉네임·모국어)
    @Operation(
            summary = "프로필 수정 (닉네임 · 모국어)",
            description = """
                    닉네임과 모국어를 수정합니다.

                    **바꾸지 않을 항목은 null로 보내거나 아예 빼면 기존 값이 유지됩니다.**
                    예를 들어 닉네임만 바꾸려면 `{ "nickname": "새닉네임" }`만 보내면 됩니다.

                    프로필 이미지는 파일이라 별도 API(`POST /users/me/profile-image`)를 사용하세요.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`"),
            @ApiResponse(responseCode = "404", description = "`USER_NOT_FOUND` 존재하지 않는 회원"),
            @ApiResponse(responseCode = "409", description = "`NICKNAME_ALREADY_EXISTS` 이미 사용 중인 닉네임")
    })
    @PatchMapping("/me")
    public ResponseEntity<RsData<String>> updateProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserUpdateRequestDTO request) {
        userProfileService.updateProfile(userId, request);
        return ResponseEntity.ok(RsData.success("프로필이 수정되었습니다."));
    }

    // 프로필 이미지 업로드 (multipart/form-data)
    // JSON이 아니라 파일이므로 @RequestBody가 아닌 @RequestPart로 받는다
    @Operation(
            summary = "프로필 이미지 업로드",
            description = """
                    **`multipart/form-data`로 보내야 합니다.** (JSON 아님)

                    - `image` (필수): 이미지 파일. jpg · jpeg · png · gif, 최대 50MB

                    응답의 `data`에 저장된 이미지 URL이 문자열로 담겨 옵니다. 그대로 화면에 반영하세요.
                    기존 이미지가 있으면 교체됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드 성공 — data에 이미지 URL"),
            @ApiResponse(responseCode = "400",
                    description = "`EMPTY_FILE` 파일이 비어 있음 / `INVALID_FILE_TYPE` 허용되지 않는 확장자"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`"),
            @ApiResponse(responseCode = "500", description = "`FILE_UPLOAD_FAILED` 저장 중 오류")
    })
    @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RsData<String>> uploadProfileImage(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "업로드할 이미지 파일 (jpg · jpeg · png · gif)")
            @RequestPart("image") MultipartFile image) {
        String imageUrl = userProfileService.updateProfileImage(userId, image);
        return ResponseEntity.ok(RsData.success(imageUrl)); // 저장된 이미지 URL 반환
    }

    // 비밀번호 변경
    @Operation(
            summary = "비밀번호 변경",
            description = """
                    기존 비밀번호 확인 후 변경합니다. 세 필드 모두 필수입니다.

                    **소셜 로그인으로 가입한 회원은 비밀번호가 없어 호출할 수 없습니다**
                    (`SOCIAL_USER_PASSWORD_NOT_ALLOWED`). 마이페이지에서 해당 메뉴를 숨기려면
                    회원 조회 응답을 활용하세요.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400",
                    description = "`PASSWORD_CONFIRM_NOT_MATCHED` 새 비밀번호와 확인 불일치 / "
                            + "`INVALID_PASSWORD_FORMAT` 8자 미만 / "
                            + "`SOCIAL_USER_PASSWORD_NOT_ALLOWED` 소셜 회원"),
            @ApiResponse(responseCode = "401",
                    description = "`PASSWORD_NOT_MATCHED` 기존 비밀번호 불일치 / `TOKEN_EXPIRED` / `TOKEN_INVALID`"),
            @ApiResponse(responseCode = "404", description = "`USER_NOT_FOUND` 존재하지 않는 회원")
    })
    @PatchMapping("/me/password")
    public ResponseEntity<RsData<String>> changePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PasswordChangeRequestDTO request) {
        userProfileService.changePassword(userId, request);
        return ResponseEntity.ok(RsData.success("비밀번호가 변경되었습니다."));
    }
}
