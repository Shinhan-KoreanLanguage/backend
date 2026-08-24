package com.daehanforeigner.capstone.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

// 로컬에 저장한 파일을 URL로 접근할 수 있게 매핑.
// "/images/** 요청 → 로컬 uploads 폴더에서 파일을 찾아 응답"
// (추후 S3로 바꾸면 파일이 S3 URL로 직접 열리므로 이 설정은 불필요)
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${custom.file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // uploads 폴더 절대경로를 file: URI로 (끝에 / 필수)
        String uploadPath = Paths.get(uploadDir).toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/images/**")   // 이 URL로 오면
                .addResourceLocations(
                        uploadPath,                        // 1순위: 업로드된 파일 (uploads 폴더)
                        // 2순위: jar에 포함된 기본 이미지.
                        // uploads 폴더는 .gitignore 대상이라 배포 서버에는 존재하지 않는다.
                        // 기본 프로필처럼 항상 있어야 하는 파일은 여기에 두어야 404가 나지 않는다
                        "classpath:/static/images/");
    }
}
