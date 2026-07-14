package com.daehanforeigner.capstone.global.oauth.client;

import com.daehanforeigner.capstone.domain.social_account.entity.Provider;
import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// "GOOGLE 요청이면 GoogleOAuthClient, FACEBOOK이면 FacebookOAuthClient" — 담당자 배정 창구
@Component
public class OAuthClientFactory {

    private final Map<Provider, OAuthClient> clients;

    // 스프링은 List<OAuthClient> 파라미터를 보면 OAuthClient 구현 빈을 전부 모아서 주입해줌
    // → {GOOGLE: GoogleOAuthClient, FACEBOOK: FacebookOAuthClient} 조회표가 자동으로 완성됨
    public OAuthClientFactory(List<OAuthClient> clientList) {
        this.clients = clientList.stream()
                .collect(Collectors.toMap(OAuthClient::getProvider, Function.identity()));
    }

    public OAuthClient getClient(Provider provider) {
        OAuthClient client = clients.get(provider);
        if (client == null) { // enum에는 있지만 담당 클라이언트가 없는 경우
            throw new CustomException(ErrorCode.UNSUPPORTED_SOCIAL_PROVIDER);
        }
        return client;
    }
}