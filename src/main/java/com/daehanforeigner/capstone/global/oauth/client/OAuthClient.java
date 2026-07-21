package com.daehanforeigner.capstone.global.oauth.client;

import com.daehanforeigner.capstone.domain.social_account.entity.Provider;
import com.daehanforeigner.capstone.global.oauth.dto.OAuthUserInfo;

// 소셜 클라이언트 인터페이스
public interface OAuthClient {

    Provider getProvider(); // 어떤 소셜 플랫폼인지

    OAuthUserInfo getUserInfo(String code); // 인가코드 통해 사용자 정보 가져오기

    String generateLoginUrl(); // 소셜 로그인 페이지로 보낼 인가 URL 생성 (테스트·프론트 공용)
}
