package com.daehanforeigner.capstone.domain.user.dto.profile;

import com.daehanforeigner.capstone.domain.user.entity.NativeLanguage;

// 프로필 텍스트 정보 수정 요청 (닉네임·모국어).
// 프로필 이미지는 파일 업로드 API(POST /me/profile-image)로 별도 처리하므로 여기 없음.
public record UserUpdateRequestDTO(
        String nickname,
        NativeLanguage nativeLanguage
) {
}
