package com.daehanforeigner.capstone.domain.user.service.profile;

import com.daehanforeigner.capstone.domain.user.dto.profile.UserProfileResponseDTO;
import com.daehanforeigner.capstone.domain.user.entity.User;
import com.daehanforeigner.capstone.domain.user.repository.UserRepository;
import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import com.daehanforeigner.capstone.global.storage.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final FileService fileService;   // 파일 저장 담당

    // 프로필 미설정 시 반환할 기본 이미지 URL (final 아닌 필드에 @Value 직접 주입)
    @Value("${custom.file.default-profile-image}")
    private String defaultProfileImage;

    // 사용자 프로필 조회
    public UserProfileResponseDTO getMyProfile(Long userId) {
        User user = findUser(userId);
        return UserProfileResponseDTO.from(user, defaultProfileImage);
    }

    // 프로필 이미지 업로드 → 로컬 저장 후 URL을 DB에 반영
    @Transactional
    public String updateProfileImage(Long userId, MultipartFile image) {
        User user = findUser(userId);

        String imageUrl = fileService.saveImage(image, "profile"); // 저장 후 URL 반환
        user.updateProfile(null, imageUrl, null); // 이미지 URL만 갱신

        return imageUrl;
    }

    // 공통: 회원 조회 (없으면 예외)
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
