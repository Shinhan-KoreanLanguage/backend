package com.daehanforeigner.capstone.domain.learning_content.service;

import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContentTranslation;
import com.daehanforeigner.capstone.domain.learning_content.repository.LearningContentTranslationRepository;
import com.daehanforeigner.capstone.domain.user.entity.NativeLanguage;
import com.daehanforeigner.capstone.domain.user.entity.User;
import com.daehanforeigner.capstone.domain.user.repository.UserRepository;
import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 회원 모국어 기준 콘텐츠 번역 조회. 학습 목록·오답 목록 등 여러 화면이 같은 규칙
// (모국어 우선, 없으면 영어 대체)을 쓰므로 공용으로 뽑았다
@Component
@RequiredArgsConstructor
public class ContentTranslationResolver {

    // 회원 모국어 번역이 없을 때 대신 보여줄 언어
    private static final NativeLanguage FALLBACK_LANGUAGE = NativeLanguage.EN;

    private final UserRepository userRepository;

    private final LearningContentTranslationRepository translationRepository;

    // 회원의 모국어. 모국어를 지정하지 않은 회원은 대체 언어로 본다
    public NativeLanguage resolveLanguage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return user.getNativeLanguage() != null ? user.getNativeLanguage() : FALLBACK_LANGUAGE;
    }

    // 콘텐츠별로 보여줄 번역을 고른다.
    // 모국어 번역이 없으면 영어로 대체하고, 그마저 없으면 담지 않는다 (호출부에서 null 처리)
    public Map<Long, LearningContentTranslation> resolve(List<LearningContent> contents, NativeLanguage language) {
        Map<Long, LearningContentTranslation> result = new HashMap<>();

        // EnumSet을 쓰는 이유: 모국어가 영어면 Set.of(EN, EN)이 되어 중복 원소 예외가 난다
        for (LearningContentTranslation translation :
                translationRepository.findAllByLearningContentInAndLanguageIn(
                        contents, EnumSet.of(language, FALLBACK_LANGUAGE))) {

            Long contentId = translation.getLearningContent().getContentId();
            LearningContentTranslation selected = result.get(contentId);

            // 모국어 번역이 대체 언어보다 우선한다. 조회 순서를 보장할 수 없어 매번 비교한다
            if (selected == null || translation.getLanguage() == language) {
                result.put(contentId, translation);
            }
        }

        return result;
    }
}
