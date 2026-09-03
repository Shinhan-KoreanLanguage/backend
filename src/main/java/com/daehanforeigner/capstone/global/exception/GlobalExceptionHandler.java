package com.daehanforeigner.capstone.global.exception;

import com.daehanforeigner.capstone.global.rsdata.RsData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.Arrays;

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
                .body(RsData.fail(ErrorCode.BAD_REQUEST, resolveMessage(e)));
    }

    // 어느 필드가 잘못됐는지 알려준다.
    // "잘못된 요청입니다."만 내려가면 프론트가 원인을 찾을 수 없다
    private String resolveMessage(HttpMessageNotReadableException e) {
        if (e.getCause() instanceof InvalidFormatException cause && !cause.getPath().isEmpty()) {
            String field = cause.getPath().get(cause.getPath().size() - 1).getPropertyName();
            Class<?> type = cause.getTargetType();

            // enum이면 어떤 값을 넣어야 하는지까지 알려준다
            if (type != null && type.isEnum()) {
                return "%s 값이 올바르지 않습니다. 가능한 값: %s"
                        .formatted(field, Arrays.toString(type.getEnumConstants()));
            }
            return "%s 값의 형식이 올바르지 않습니다.".formatted(field);
        }

        return ErrorCode.BAD_REQUEST.getMessage();
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

    // 필수로 지정한 @RequestPart(예: 발음 게임 단어 제출의 audio)가 multipart 본문 안에는
    // 없는 경우 → 컨트롤러 진입 전에 터지므로 서비스단의 null 체크가 실행되지 않는다.
    //   이게 없으면 아래 Exception 핸들러가 잡아 500이 남
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<RsData<Void>> handleMissingPart(MissingServletRequestPartException e) {
        return ResponseEntity
                .status(ErrorCode.EMPTY_FILE.getHttpStatus())
                .body(RsData.fail(ErrorCode.EMPTY_FILE));
    }

    // 업로드 파일이 서블릿 단계의 크기 제한(spring.servlet.multipart)을 넘은 경우.
    // 이 예외는 컨트롤러에 도달하기 전에 터지므로 FileService의 용도별 검사로는 잡을 수 없다.
    // 핸들러가 없으면 500이 되어 서버 장애로 오해하게 되므로 413으로 명확히 알려준다.
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<RsData<Void>> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return ResponseEntity
                .status(ErrorCode.FILE_SIZE_EXCEEDED.getHttpStatus())
                .body(RsData.fail(ErrorCode.FILE_SIZE_EXCEEDED));
    }

    // multipart/form-data가 필요한 요청에 본문을 아예 안 보내거나 다른 Content-Type으로 보낸 경우
    // ex) 발음 게임 단어 제출 시 audio 파일 자체를 첨부하지 않고 요청한 경우
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<RsData<Void>> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException e) {
        return ResponseEntity
                .status(ErrorCode.EMPTY_FILE.getHttpStatus())
                .body(RsData.fail(ErrorCode.EMPTY_FILE));
    }

    // 경로는 맞지만 지원하지 않는 메서드로 요청한 경우 ex) 조회 전용 경로에 PUT
    // → 이게 없으면 500이 되어 서버 장애로 오해하게 된다. 실제로는 URL을 잘못 부른 것이다
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<RsData<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity
                .status(ErrorCode.METHOD_NOT_ALLOWED.getHttpStatus())
                .body(RsData.fail(ErrorCode.METHOD_NOT_ALLOWED));
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
