package com.daehanforeigner.capstone.domain.user.controller;

import com.daehanforeigner.capstone.domain.user.dto.profile.UserProfileResponseDTO;
import com.daehanforeigner.capstone.domain.user.service.profile.UserProfileService;
import com.daehanforeigner.capstone.global.rsdata.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<RsData<UserProfileResponseDTO>> getMyProfile(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(RsData.success(userProfileService.getMyProfile(userId)));
    }

    // 프로필 이미지 업로드 (multipart/form-data)
    // JSON이 아니라 파일이므로 @RequestBody가 아닌 @RequestPart로 받는다
    @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RsData<String>> uploadProfileImage(
            @AuthenticationPrincipal Long userId,
            @RequestPart("image") MultipartFile image) {
        String imageUrl = userProfileService.updateProfileImage(userId, image);
        return ResponseEntity.ok(RsData.success(imageUrl)); // 저장된 이미지 URL 반환
    }
}
