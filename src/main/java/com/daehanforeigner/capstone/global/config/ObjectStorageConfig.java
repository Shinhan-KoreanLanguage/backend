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
                // AWS SDK 2.30부터 업로드 시 CRC32 체크섬을 기본으로 붙이는데(aws-chunked 인코딩),
                // NCP Object Storage는 이를 해석하지 못해 PutObject가 403 Access Denied로 거부된다.
                // 권한 문제로 보이지만 실제로는 요청 형식 문제라 원인을 찾기 어려우니 주의.
                // 필요한 경우에만 체크섬을 쓰도록 낮춰서 NCP가 이해하는 표준 요청으로 보낸다.
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .build();
    }
}
