package com.daehanforeigner.capstone.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

// NCP Object Storage 연결 설정.
// NCP는 S3 호환 API를 제공하므로 AWS SDK를 그대로 쓰되,
// 접속 주소만 NCP 엔드포인트로 바꿔주면 된다.
@Configuration
public class ObjectStorageConfig {

    @Value("${custom.ncp.storage.endpoint}")
    private String endpoint;

    @Value("${custom.ncp.storage.region}")
    private String region;

    @Value("${custom.ncp.storage.access-key}")
    private String accessKey;

    @Value("${custom.ncp.storage.secret-key}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                // AWS가 아닌 NCP 주소로 요청을 보내도록 지정
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                // 기본값(virtual-host 방식)은 버킷명을 도메인 앞에 붙이는데,
                // NCP는 "엔드포인트/버킷명/파일경로" 형태를 쓰므로 path 방식으로 고정한다
                .forcePathStyle(true)
                // AWS SDK 2.30+는 업로드 시 본문을 aws-chunked로 쪼개고 끝에 체크섬을 붙이는데,
                // NCP가 이 형식을 해석하지 못해 403이 난다. 꼭 필요할 때만 붙이도록 낮춘다
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .build();
    }
}
