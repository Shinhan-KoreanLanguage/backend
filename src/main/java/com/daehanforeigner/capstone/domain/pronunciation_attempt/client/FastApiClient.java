package com.daehanforeigner.capstone.domain.pronunciation_attempt.client;

import com.daehanforeigner.capstone.domain.pronunciation_attempt.dto.PronunciationAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;

@Component
@RequiredArgsConstructor
public class FastApiClient {

    private final RestTemplate fastApiRestTemplate;

    @Value("${fastapi.base-url}")
    private String baseUrl;

    public PronunciationAnalysisResponse analyze(MultipartFile audio, String targetText, MultipartFile video, MultipartFile referenceVideo) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("audio", toResource(audio));
        body.add("target_text", targetText);
        if (video != null && !video.isEmpty()) {
            body.add("video", toResource(video));
        }
        if (referenceVideo != null && !referenceVideo.isEmpty()) {
            body.add("reference_video", toResource(referenceVideo));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        return fastApiRestTemplate.postForObject(
                baseUrl + "/api/v1/pronunciation/analyze",
                request,
                PronunciationAnalysisResponse.class
        );
    }

    private ByteArrayResource toResource(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            return new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
