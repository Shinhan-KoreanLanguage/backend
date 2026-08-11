package com.daehanforeigner.capstone.domain.admin.service;

import com.daehanforeigner.capstone.domain.admin.dto.AdminProfileResponseDTO;
import com.daehanforeigner.capstone.domain.content_category.dto.CategoryResponseDTO;
import com.daehanforeigner.capstone.domain.content_category.entity.ContentCategory;
import com.daehanforeigner.capstone.domain.content_category.repository.ContentCategoryRepository;
import com.daehanforeigner.capstone.domain.user.entity.User;
import com.daehanforeigner.capstone.domain.user.repository.UserRepository;
import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;

    private final ContentCategoryRepository contentCategoryRepository;

    // 관리자 정보 조회
    public AdminProfileResponseDTO getAdminProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return AdminProfileResponseDTO.from(user);
    }

    // 카테고리 전체 조회
    public List<CategoryResponseDTO> getCategories() {
        List<ContentCategory> contentCategories = contentCategoryRepository.findAll();

        return contentCategories.stream()
                .map(contentCategory -> CategoryResponseDTO.from(contentCategory))
                .toList();
    }
}
