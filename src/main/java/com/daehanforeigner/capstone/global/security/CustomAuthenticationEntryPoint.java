package com.daehanforeigner.capstone.global.security;

import com.daehanforeigner.capstone.global.exception.ErrorCode;
import com.daehanforeigner.capstone.global.rsdata.RsData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

// 시큐리티 필터 체인에서 터지는 인증 실패는 컨트롤러 이전이라 @RestControllerAdvice가 못 잡음
// → 시큐리티가 제공하는 이 훅에서 직접 JSON을 써서 응답
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper; // 스프링이 관리하는 ObjectMapper 주입

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        // ResponseEntity를 못 쓰는 위치라 RsData를 직접 직렬화해서 body에 씀
        response.getWriter().write(objectMapper.writeValueAsString(RsData.fail(errorCode)));
    }
}