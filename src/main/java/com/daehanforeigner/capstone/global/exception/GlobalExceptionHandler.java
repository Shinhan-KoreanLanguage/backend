package com.daehanforeigner.capstone.global.exception;

import com.daehanforeigner.capstone.global.rsdata.RsData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j // log 객체 생성해서 로그를 남길 수 있도록 함
@RestControllerAdvice // 모든 컨트롤러에서 공통으로 발생하는 예외처리를 이 곳에서 처리해 JSON 형태로 응답
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class) // CustomException이 발생했을 때
    public ResponseEntity<RsData<Void>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(RsData.fail(errorCode, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class) // 요청값 검증 실패했을 때 ex) 컨트롤러에서 @Valid 붙은 DTO 검증 실패했을 때
    public ResponseEntity<RsData<Void>> handleValidationException(MethodArgumentNotValidException e) {

        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse(ErrorCode.BAD_REQUEST.getMessage());
        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getHttpStatus())
                .body(RsData.fail(ErrorCode.BAD_REQUEST, message));
    }

    // 요청 본문(JSON) 자체를 못 읽는 경우 ex) enum에 잘못된 값, 타입 불일치, 깨진 JSON
    // → 컨트롤러 진입 전에 터지므로 @Valid와 별개. 이게 없으면 아래 Exception 핸들러가 잡아 500이 남
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RsData<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getHttpStatus())
                .body(RsData.fail(ErrorCode.BAD_REQUEST));
    }

    @ExceptionHandler(Exception.class) // 그 외 모든 예외처리
    public ResponseEntity<RsData<Void>> handleException(Exception e) {
        log.error("처리되지 않은 예외 발생: {}", e);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(RsData.fail(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
