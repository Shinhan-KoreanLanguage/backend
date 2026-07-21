package com.daehanforeigner.capstone.domain.user.service.profile;

import com.daehanforeigner.capstone.domain.user.dto.profile.PasswordChangeRequestDTO;
import com.daehanforeigner.capstone.domain.user.dto.profile.UserProfileResponseDTO;
import com.daehanforeigner.capstone.domain.user.dto.profile.UserUpdateRequestDTO;
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

    // 공통: 회원 조회 (없으면 예외)
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    // 사용자 프로필 조회
    public UserProfileResponseDTO getMyProfile(Long userId) {
        User user = findUser(userId);
        return UserProfileResponseDTO.from(user, defaultProfileImage);
    }

    // 프로필 텍스트 수정 (닉네임·모국어)
    @Transactional
    public void updateProfile(Long userId, UserUpdateRequestDTO request) {
        User user = findUser(userId);

        // 닉네임을 바꾸는 경우에만 중복 검사 (안 보내면 null → 통과)
        if (request.nickname() != null && userRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        // 이미지는 별도 API 담당이라 null → updateProfile이 이미지를 건드리지 않음
        user.updateProfile(request.nickname(), null, request.nativeLanguage());
    }

    // 프로필 이미지 업로드 → 로컬 저장 후 URL을 DB에 반영
    @Transactional
    public String updateProfileImage(Long userId, MultipartFile image) {
        User user = findUser(userId);

        String imageUrl = fileService.saveImage(image, "profile"); // 저장 후 URL 반환
        user.updateProfile(null, imageUrl, null); // 이미지 URL만 갱신

        return imageUrl;
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequestDTO request) {
        User user = findUser(userId);

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCHED);
        }

        // 새 비밀번호와 확인 비밀번호 일치 여부 확인
        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_CONFIRM_NOT_MATCHED);
        }
        user.changePassword(passwordEncoder.encode(request.newPassword()));
    }
}
