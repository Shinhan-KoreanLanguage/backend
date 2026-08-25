package com.daehanforeigner.capstone.global.storage;

import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

// 파일 저장 담당 (NCP Object Storage).
// 저장 위치가 바뀌어도 호출하는 쪽은 그대로 쓸 수 있도록 메서드 형태는 유지한다.
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final S3Client s3Client;

    @Value("${custom.ncp.storage.bucket}")
    private String bucket;

    @Value("${custom.ncp.storage.endpoint}")
    private String endpoint;

    // 허용 확장자 (이미지만)
    private static final List<String> IMAGE_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif"); // 이미지 (프로필)

    private static final List<String> AUDIO_EXTENSIONS = List.of("mp3", "wav", "m4a", "webm"); // 오디오 (발음) — webm은 브라우저 MediaRecorder 기본 출력

    private static final List<String> VIDEO_EXTENSIONS = List.of("mp4","webm", "avi", "mov"); // 비디오 (학습 영상)

    // 이미지 저장
    public String saveImage(MultipartFile file, String directory) {
        return save(file, directory, IMAGE_EXTENSIONS);
    }

    // 오디오 저장
    public String saveAudio(MultipartFile file, String directory) {
        return save(file, directory, AUDIO_EXTENSIONS);
    }

    // 비디오 저장
    public String saveVideo(MultipartFile file, String directory) {
        return save(file, directory, VIDEO_EXTENSIONS);
    }

    // 저장된 파일을 다시 읽어온다 (AI 분석 서버로 재전송할 때 사용).
    public Resource loadAsResource(String fileUrl) {
        String key = extractKey(fileUrl);

        try {
            byte[] bytes = s3Client.getObject(
                    GetObjectRequest.builder().bucket(bucket).key(key).build(),
                    ResponseTransformer.toBytes()).asByteArray();

            // AI 서버로 multipart 전송할 때 파일명이 필요하므로 함께 담아준다
            String fileName = key.substring(key.lastIndexOf('/') + 1);
            return new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };

        } catch (NoSuchKeyException e) {
            // DB에는 URL이 남았지만 버킷에서 파일이 지워진 경우
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        } catch (S3Exception e) {
            log.error("Object Storage 조회 실패 (bucket={}, key={})", bucket, key, e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    // 저장된 URL에서 버킷 내 경로(key)만 뽑아낸다.
    // 예) https://kr.object.ncloudstorage.com/버킷명/audio/uuid.mp3 → audio/uuid.mp3
    private String extractKey(String fileUrl) {
        String urlPrefix = endpoint + "/" + bucket + "/";

        // 우리가 저장한 URL이 맞는지 확인 (외부 URL·null 차단)
        if (fileUrl == null || !fileUrl.startsWith(urlPrefix)) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        return fileUrl.substring(urlPrefix.length());
    }

    private String save(MultipartFile file, String directory, List<String> allowedExtensions) {
        // 1. 빈 파일 검증
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_FILE);
        }

        // 2. 확장자 검증 (용도별 허용 목록과 대조)
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension == null || !allowedExtensions.contains(extension.toLowerCase())) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }

        // 3. 저장 경로 생성 (UUID로 중복·한글파일명 문제 방지)
        String key = directory + "/" + UUID.randomUUID() + "." + extension.toLowerCase();

        try {
            // 4. 버킷에 업로드
            //    - publicRead: 프론트가 URL로 바로 열 수 있어야 하므로 공개 읽기로 저장
            //    - contentType: 지정하지 않으면 브라우저가 이미지·음성을 재생하지 못하고 다운로드해버린다
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .acl(ObjectCannedACL.PUBLIC_READ)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // 5. 접근 URL 반환 (예: https://kr.object.ncloudstorage.com/버킷명/audio/uuid.mp3)
            return endpoint + "/" + bucket + "/" + key;

        } catch (IOException | S3Exception e) {
            // 원인을 남기지 않으면 버킷·권한·자격증명 중 무엇이 문제인지 알 수 없다
            log.error("Object Storage 업로드 실패 (bucket={}, key={})", bucket, key, e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }
}
