package com.daehanforeigner.capstone.global.storage;

import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

// 파일 저장 담당.
// 지금은 로컬 폴더에 저장하지만, 추후 S3로 바꿀 때는 이 클래스 내용만 수정하면 된다.
@Service
public class FileService {

    // 로컬 저장 폴더 (application.yml의 custom.file.upload-dir)
    @Value("${custom.file.upload-dir}")
    private String uploadDir;

    // 저장된 파일에 접근할 URL 앞부분 (application.yml의 custom.file.url-prefix)
    @Value("${custom.file.url-prefix}")
    private String urlPrefix;

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
        // 1. 우리가 저장한 URL이 맞는지 확인 (외부 URL·null 차단)
        if (fileUrl == null || !fileUrl.startsWith(urlPrefix)) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        // 2. urlPrefix를 떼면 "/audio/uuid.mp3" 형태가 남으므로 앞의 "/"까지 제거
        String relativePath = fileUrl.substring(urlPrefix.length()).replaceFirst("^/", "");

        // 3. 저장 루트(uploadDir) 기준으로 실제 경로 조립
        Path path = Paths.get(uploadDir).toAbsolutePath().resolve(relativePath);

        // 4. 파일이 실제로 존재하는지 확인 (DB에는 URL이 남았지만 파일이 지워진 경우 방어)
        if (!Files.exists(path)) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        return new FileSystemResource(path);
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

        // 3. 저장 파일명 생성 (UUID로 중복·한글파일명 문제 방지)
        String storedFileName = UUID.randomUUID() + "." + extension.toLowerCase();

        try {
            // 4. 저장 폴더(uploadDir/directory) 없으면 생성
            Path directoryPath = Paths.get(uploadDir, directory).toAbsolutePath();
            Files.createDirectories(directoryPath);

            // 5. 실제 파일 저장
            file.transferTo(directoryPath.resolve(storedFileName).toFile());

            // 6. 접근 URL 반환 (예: http://localhost:8080/images/audio/uuid.mp3)
            return urlPrefix + "/" + directory + "/" + storedFileName;

        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }
}