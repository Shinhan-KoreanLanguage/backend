package com.daehanforeigner.capstone.global.exception;

import com.daehanforeigner.capstone.global.rsdata.RsData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

    @ExceptionHandler(Exception.class) // 그 외 모든 예외처리
    public ResponseEntity<RsData<Void>> handleException(Exception e) {
        log.error("처리되지 않은 예외 발생: {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(RsData.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage()));
}
