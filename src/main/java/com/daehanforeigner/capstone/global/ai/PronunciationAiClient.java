package com.daehanforeigner.capstone.global.ai;

import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

// 발음 분석 AI 서버(FastAPI)와의 통신 담당
@Component
public class PronunciationAiClient {

    private static final String REFERENCE_URI = "/api/v1/pronunciation/reference";

    private static final String ANALYZE_URI = "/api/v1/pronunciation/analyze";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final RestClient restClient;

    public PronunciationAiClient(@Value("${custom.ai.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));  // 서버가 내려가 있으면 빨리 실패시킨다
        factory.setReadTimeout(Duration.ofSeconds(60));    // 분석 자체는 오래 걸릴 수 있음

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    // 원어민 기준 등록 — 관리자가 콘텐츠를 등록·수정할 때 호출.
    // 이게 선행되어야 analyze가 기준을 찾을 수 있다 (같은 text면 덮어씀)
    public JsonNode registerReference(String text, Resource video, Resource audio) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("text", text);
        body.add("video", video); // 필수 — 입모양 랜드마크 추출용

        if (audio != null) {
            body.add("audio", audio); // 선택 — 안 보내면 피치가 저장되지 않는다
        }

        return OBJECT_MAPPER.readTree(postMultipart(REFERENCE_URI, body));
    }

    // 원어민 피치 곡선 조회 — 등록 직후 native_pitch_data에 캐시할 용도
    public JsonNode getReference(String text) {
        try {
            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(REFERENCE_URI)
                            .queryParam("text", text) // 한글은 여기서 자동 인코딩된다
                            .build())
                    .retrieve()
                    .body(String.class);

            return OBJECT_MAPPER.readTree(response);

        } catch (RestClientResponseException e) {
            throw toCustomException(e);

        } catch (ResourceAccessException e) {
            throw new CustomException(ErrorCode.AI_SERVER_ERROR);
        }
    }

    // 발음 분석 — 사용자 녹음 업로드 시 호출
    public JsonNode analyze(String targetText, Resource audio, Resource video) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("audio", audio);            // 필수 — 학습자가 녹음한 발화
        body.add("target_text", targetText); // 필수 — 원어민 기준을 찾는 키로도 쓰인다

        if (video != null) {
            body.add("video", video); // 선택 — 없으면 입모양 점수가 null로 온다
        }

        return OBJECT_MAPPER.readTree(postMultipart(ANALYZE_URI, body));
    }

    private String postMultipart(String uri, MultiValueMap<String, Object> body) {
        try {
            return restClient.post()
                    .uri(uri)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(String.class);

        } catch (RestClientResponseException e) {
            throw toCustomException(e);

        } catch (ResourceAccessException e) {
            // 연결 실패·타임아웃 (서버가 내려가 있는 경우)
            throw new CustomException(ErrorCode.AI_SERVER_ERROR);
        }
    }

    // 404는 "서버 장애"가 아니라 "원어민 기준 미등록"이라 조치가 다르므로 구분한다
    private CustomException toCustomException(RestClientResponseException e) {
        if (e.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
            return new CustomException(ErrorCode.AI_REFERENCE_NOT_FOUND);
        }
        return new CustomException(ErrorCode.AI_SERVER_ERROR);
    }
}