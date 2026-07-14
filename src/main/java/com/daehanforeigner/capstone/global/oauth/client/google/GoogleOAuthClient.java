package com.daehanforeigner.capstone.global.oauth.client.google;

import com.daehanforeigner.capstone.domain.social_account.entity.Provider;
import com.daehanforeigner.capstone.global.oauth.OAuthProperties;
import com.daehanforeigner.capstone.global.oauth.client.AbstractOAuthClient;
import com.daehanforeigner.capstone.global.oauth.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Component // 빈 등록 → OAuthClientFactory의 List<OAuthClient>에 자동으로 수집됨
@RequiredArgsConstructor
public class GoogleOAuthClient extends AbstractOAuthClient {

    // URL은 구글이 정한 고정값이라 설정이 아닌 상수로 관리
    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URI = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final OAuthProperties properties;

    @Override
    public Provider getProvider() {
        return Provider.GOOGLE; // Factory가 "GOOGLE 요청 → 이 클래스"로 매핑할 때 사용
    }

    @Override
    public OAuthUserInfo getUserInfo(String code) {
        OAuthProperties.Registration registration = properties.getRegistration(Provider.GOOGLE);

        // [1단계] 인가 코드 → 구글 액세스 토큰 교환 (구글 공식 문서의 필수 파라미터 5종)
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", registration.clientId());
        form.add("client_secret", registration.clientSecret()); // 이 값 때문에 교환은 서버에서만 가능
        form.add("redirect_uri", registration.redirectUri());   // 인가 요청 때 값과 일치해야 함
        form.add("grant_type", "authorization_code");

        Map<String, Object> token = postForToken(TOKEN_URI, form);

        // [2단계] 액세스 토큰 → 사용자 정보 조회
        Map<String, Object> attributes = getUserAttributes(USER_INFO_URI, stringValue(token, "access_token"));

        // [3단계] 구글 필드명(sub/email/name)을 우리 공통 모델로 변환
        return new OAuthUserInfo(
                Provider.GOOGLE,
                stringValue(attributes, "sub"),
                stringValue(attributes, "email"),
                stringValue(attributes, "name")
        );
    }
}