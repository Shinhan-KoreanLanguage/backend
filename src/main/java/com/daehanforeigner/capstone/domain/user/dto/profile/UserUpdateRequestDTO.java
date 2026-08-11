package com.daehanforeigner.capstone.domain.user.dto.profile;

import com.daehanforeigner.capstone.domain.user.entity.NativeLanguage;
import io.swagger.v3.oas.annotations.media.Schema;

// 프로필 텍스트 정보 수정 요청 (닉네임·모국어).
// 프로필 이미지는 파일 업로드 API(POST /me/profile-image)로 별도 처리하므로 여기 없음.
@Schema(description = "프로필 수정 요청 — 바꿀 항목만 보내면 나머지는 기존 값이 유지됩니다")
public record UserUpdateRequestDTO(
        @Schema(description = "새 닉네임 (변경하지 않으려면 생략)", example = "새닉네임")
        String nickname,

        @Schema(description = "새 모국어 (변경하지 않으려면 생략)",
                example = "EN", allowableValues = {"KR", "EN", "JP", "CN"})
        NativeLanguage nativeLanguage
) {
}
