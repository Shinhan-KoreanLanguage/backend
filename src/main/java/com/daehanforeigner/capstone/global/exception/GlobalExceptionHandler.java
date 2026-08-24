package com.daehanforeigner.capstone.global.exception;

import com.daehanforeigner.capstone.global.rsdata.RsData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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

    // 존재하지 않는 필드로 정렬을 요청한 경우 ex) ?sort=wrongcount (오타)
    // → 사용자 입력 실수이므로 500이 아니라 400으로 응답한다.
    //   이게 없으면 아래 Exception 핸들러가 잡아 서버 오류로 보임
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<RsData<Void>> handleInvalidSort(PropertyReferenceException e) {
        return ResponseEntity
                .status(ErrorCode.INVALID_SORT_PROPERTY.getHttpStatus())
                .body(RsData.fail(ErrorCode.INVALID_SORT_PROPERTY));
    }

    // 매핑되지 않은 경로를 요청한 경우 ex) 오타, 아직 배포되지 않은 API 호출
    // → 이게 없으면 아래 Exception 핸들러가 잡아 500이 되어, 프론트가 "서버가 죽었나" 오해하게 된다
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RsData<Void>> handleNoResourceFound(NoResourceFoundException e) {
        return ResponseEntity
                .status(ErrorCode.NOT_FOUND.getHttpStatus())
                .body(RsData.fail(ErrorCode.NOT_FOUND));
    }

    @ExceptionHandler(Exception.class) // 그 외 모든 예외처리
    public ResponseEntity<RsData<Void>> handleException(Exception e) {
        log.error("처리되지 않은 예외 발생: {}", e);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(RsData.fail(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
