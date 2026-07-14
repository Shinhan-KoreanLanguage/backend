package com.daehanforeigner.capstone.global.oauth.client;

import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

// 구글/페이스북 클라이언트가 공통으로 쓰는 HTTP 통신 부품 모음.
// 자식 클래스는 "무엇을 보낼지(파라미터)"와 "응답에서 뭘 꺼낼지"만 담당하고,
// "어떻게 보낼지(HTTP)"는 전부 여기에 위임한다
public abstract class AbstractOAuthClient implements OAuthClient {

    protected final RestClient restClient = RestClient.create(); // 스프링 공식 HTTP 클라이언트

    // 응답을 직접 파싱하기 위한 JSON 파서.
    // 페이스북이 토큰 응답의 Content-Type을 text/javascript로 보내는 탓에
    // RestClient의 자동 변환(application/json만 인식)이 실패함
    // → 응답을 문자열로 받아서 우리가 직접 Map으로 파싱 (content type과 무관해짐)
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // 토큰 교환: POST + form 방식 (구글이 사용)
    protected Map<String, Object> postForToken(String uri, MultiValueMap<String, String> form) {
        try {
            String response = restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(String.class);
            return parseJson(response);
        } catch (RestClientResponseException e) {
            // 소셜 서버가 4xx로 거부 = 인가 코드 만료/위조/redirect_uri 불일치
            throw new CustomException(ErrorCode.INVALID_AUTHORIZATION_CODE);
        }
    }

    // 토큰 교환: GET + 쿼리 파라미터 방식 (페이스북이 사용)
    protected Map<String, Object> getForToken(String uri) {
        try {
            String response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);
            return parseJson(response);
        } catch (RestClientResponseException e) {
            throw new CustomException(ErrorCode.INVALID_AUTHORIZATION_CODE);
        }
    }

    // 사용자 정보 조회: GET + Bearer 토큰 (구글/페이스북 공통)
    protected Map<String, Object> getUserAttributes(String uri, String accessToken) {
        try {
            String response = restClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(String.class);
            return parseJson(response);
        } catch (RestClientResponseException e) {
            // 토큰 교환은 됐는데 조회가 실패한 경우라 에러코드를 구분
            throw new CustomException(ErrorCode.SOCIAL_AUTHENTICATION_FAILED);
        }
    }

    // JSON 문자열 → Map 변환 (facebook의 경우 application/json이 아닌 text/javascript로 보내서 RestClient가 자동 변환을 못하므로 ObjectMapper로 직접 파싱)
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        return OBJECT_MAPPER.readValue(json, Map.class);
    }

    // Map에서 값을 String으로 안전하게 꺼내는 유틸 (없으면 null — NPE 방지)
    protected String stringValue(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        return value != null ? String.valueOf(value) : null;
    }
}
