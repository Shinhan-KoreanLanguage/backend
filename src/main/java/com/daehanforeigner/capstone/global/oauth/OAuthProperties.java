package com.daehanforeigner.capstone.global.oauth;

import com.daehanforeigner.capstone.domain.social_account.entity.Provider;
import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "custom.oauth2") // application.yml에서 custom.oauth2로 시작하는 설정을 가져와서 매핑
public record OAuthProperties(

        // OAuth2 클라이언트 등록 정보를 담은 Map
        Map<String, Registration> registration
) {
    public record Registration(
            String clientId,
            String clientSecret,
            String redirectUri
    ) {
    }

    public Registration getRegistration(Provider provider) { // 구글, 페이스북 등록 정보 가져오기
        Registration found = registration.get(provider.name().toLowerCase()); // 현재 ENUM 타입은 대문자로 표시했으나, yml에서는 소문자로 설정했기 때문에 소문자로 변환하여 가져옴

        if (found == null) { // yml에 설정된 내용 외에 다른 provider가 들어오는 경우
            throw new CustomException(ErrorCode.UNSUPPORTED_SOCIAL_PROVIDER);
        }
        return found;
    }
}
