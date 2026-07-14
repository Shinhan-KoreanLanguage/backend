package com.daehanforeigner.capstone.global.oauth.client.facebook;

import com.daehanforeigner.capstone.domain.social_account.entity.Provider;
import com.daehanforeigner.capstone.global.oauth.OAuthProperties;
import com.daehanforeigner.capstone.global.oauth.client.AbstractOAuthClient;
import com.daehanforeigner.capstone.global.oauth.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class FacebookOAuthClient extends AbstractOAuthClient {

    // Graph API는 URL에 버전 포함 — 페이스북 앱 대시보드의 버전과 맞출 것
    private static final String TOKEN_URI = "https://graph.facebook.com/v19.0/oauth/access_token";
    // fields로 받을 항목을 명시해야 함 (안 쓰면 id, name만 옴)
    private static final String USER_INFO_URI = "https://graph.facebook.com/v19.0/me?fields=id,name,email";

    private final OAuthProperties properties;

    @Override
    public Provider getProvider() {
        return Provider.FACEBOOK;
    }

    @Override
    public OAuthUserInfo getUserInfo(String code) {
        OAuthProperties.Registration registration = properties.getRegistration(Provider.FACEBOOK);

        // [1단계] 인가 코드 → 토큰. 페이스북은 구글과 달리 GET + 쿼리 파라미터 방식
        String tokenRequestUri = UriComponentsBuilder.fromUriString(TOKEN_URI)
                .queryParam("code", code)
                .queryParam("client_id", registration.clientId())
                .queryParam("client_secret", registration.clientSecret())
                .queryParam("redirect_uri", registration.redirectUri())
                .toUriString();

        Map<String, Object> token = getForToken(tokenRequestUri);

        // [2단계] 토큰 → 사용자 정보 (구글과 동일한 방식)
        Map<String, Object> attributes = getUserAttributes(USER_INFO_URI, stringValue(token, "access_token"));

        // [3단계] 페이스북의 고유 ID 필드는 sub가 아니라 id. email은 없을 수 있음(전화번호 가입 계정)
        return new OAuthUserInfo(
                Provider.FACEBOOK,
                stringValue(attributes, "id"),
                stringValue(attributes, "email"),
                stringValue(attributes, "name")
        );
    }
}