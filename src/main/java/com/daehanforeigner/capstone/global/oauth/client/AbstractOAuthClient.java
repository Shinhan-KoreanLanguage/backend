package com.daehanforeigner.capstone.global.oauth.client;

import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

// 구글/페이스북 클라이언트가 공통으로 쓰는 HTTP 통신 부품 모음.
// 자식 클래스는 "무엇을 보낼지(파라미터)"와 "응답에서 뭘 꺼낼지"만 담당하고,
// "어떻게 보낼지(HTTP)"는 전부 여기에 위임한다
public abstract class AbstractOAuthClient implements OAuthClient {

    // JSON 응답을 Map<String, Object>로 받기 위한 타입 정보
    protected static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<>() {
    };

    protected final RestClient restClient = RestClient.create(); // 스프링 공식 HTTP 클라이언트

    // 토큰 교환: POST + form 방식 (구글이 사용)
    protected Map<String, Object> postForToken(String uri, MultiValueMap<String, String> form) {
        try {
            return restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(RESPONSE_TYPE);
        } catch (RestClientResponseException e) {
            // 소셜 서버가 4xx로 거부 = 인가 코드 만료/위조/redirect_uri 불일치
            throw new CustomException(ErrorCode.INVALID_AUTHORIZATION_CODE);
        }
    }

    // 토큰 교환: GET + 쿼리 파라미터 방식 (페이스북이 사용)
    protected Map<String, Object> getForToken(String uri) {
        try {
            return restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(RESPONSE_TYPE);
        } catch (RestClientResponseException e) {
            throw new CustomException(ErrorCode.INVALID_AUTHORIZATION_CODE);
        }
    }

    // 사용자 정보 조회: GET + Bearer 토큰 (구글/페이스북 공통)
    protected Map<String, Object> getUserAttributes(String uri, String accessToken) {
        try {
            return restClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(RESPONSE_TYPE);
        } catch (RestClientResponseException e) {
            // 토큰 교환은 됐는데 조회가 실패한 경우라 에러코드를 구분
            throw new CustomException(ErrorCode.SOCIAL_AUTHENTICATION_FAILED);
        }
    }

    // Map에서 값을 String으로 안전하게 꺼내는 유틸 (없으면 null — NPE 방지)
    protected String stringValue(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        return value != null ? String.valueOf(value) : null;
    }
}