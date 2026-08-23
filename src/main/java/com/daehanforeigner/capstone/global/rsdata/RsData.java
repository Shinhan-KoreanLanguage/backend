package com.daehanforeigner.capstone.global.rsdata;

import com.daehanforeigner.capstone.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "공통 응답 래퍼 — 모든 API 응답이 이 형태로 감싸집니다.")
public record RsData<T>(
        @Schema(description = "성공 여부", example = "true")
        boolean success,

        @Schema(description = "응답 데이터 (실패 시 null)")
        T data,

        @Schema(description = "에러 정보 (성공 시 null)")
        ErrorInfo error,

        @Schema(description = "응답 시각", example = "2026-08-11T10:00:00")
        LocalDateTime timestamp
) {
    @Schema(description = "에러 정보")
    public record ErrorInfo(
            @Schema(description = "에러 코드 — 프론트는 이 값으로 분기하세요", example = "USER_NOT_FOUND")
            String code,

            @Schema(description = "사용자에게 그대로 보여줘도 되는 메시지", example = "존재하지 않는 회원입니다.")
            String message
    ){
    }

    public static <T> RsData<T> success(T data) {
        return new RsData<>(true, data, null, LocalDateTime.now());
    }

    public static <T> RsData<T> fail(ErrorCode errorCode) {
        return fail(errorCode, errorCode.getMessage());
    }

    public static <T> RsData<T> fail(ErrorCode errorCode, String message) {
        return new RsData<>(false, null, new ErrorInfo(errorCode.getCode(), message), LocalDateTime.now());
    }
}
