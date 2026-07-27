package com.daehanforeigner.capstone.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 공통적으로 사용하는 에러코드
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "잘못된 요청입니다."), // 형식·파라미터를 잘못된 요청값으로 요청한 경우
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증되지 않은 사용자입니다."), // 토큰이 없는 상태 혹은 로그아웃 되어 있는 상태
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "권한이 없는 사용자입니다."), // 인증은 되었으나 권한이 없는 상태 ex) 일반 사용자가 관리자 페이지 접근하는 경우
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."), // 존재하지 않는 경로 접근
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."), // 예상하지 못한 서버 오류

    // 로컬 로그인 관련 에러코드
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 가입된 이메일입니다."), // 중복 된 이메일로 회원가입을 시도할 경우
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 일치하지 않습니다."), // 로컬 로그인에서 이메일 또는 비밀번호가 일치하지 않는 경우
    WITHDRAWN_USER(HttpStatus.FORBIDDEN, "WITHDRAWN_USER", "탈퇴한 사용자입니다."), // status = WITHDRAWN인 경우

    // 토큰 관련 에러
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "토큰이 만료되었습니다."), // AccessToken이 만료된 경우
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "유효하지 않은 토큰입니다."), // AccessToken이 유효하지 않은 경우
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED", "리프레시 토큰이 만료되었습니다."), // RefreshToken이 만료된 경우 (다시 로그인 진행)
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_INVALID", "유효하지 않은 리프레시 토큰입니다."), // 저장된 RefreshToken과 일치하지 않는 경우

    // 소셜 로그인 관련 에러코드
    UNSUPPORTED_SOCIAL_PROVIDER(HttpStatus.BAD_REQUEST, "UNSUPPORTED_SOCIAL_PROVIDER", "지원하지 않는 소셜 로그인 제공자입니다."), // Google, Facebook 외 다른 소셜 로그인 제공자를 요청한 경우
    INVALID_AUTHORIZATION_CODE(HttpStatus.BAD_REQUEST, "INVALID_AUTHORIZATION_CODE", "일치하지 않은 인가 코드입니다."), // 인가 코드가 일치하지 않는 경우
    SOCIAL_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "SOCIAL_AUTHENTICATION_FAILED", "소셜 로그인 인증에 실패했습니다."), // 소셜 로그인 인증에 실패한 경우
    SOCIAL_ALREADY_REGISTERED(HttpStatus.CONFLICT, "SOCIAL_ALREADY_REGISTERED", "이미 가입된 소셜 계정입니다."), // 이미 가입된 소셜 계정으로 회원가입을 시도한 경우
    SOCIAL_USER_PASSWORD_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "SOCIAL_USER_PASSWORD_NOT_ALLOWED", "소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다."), // 소셜 로그인 사용자가 비밀번호 변경을 시도한 경우

    // 회원 정보 (마이페이지) 관련 에러코드
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "존재하지 않는 회원입니다."), // userId로 회원 조회 실패 (탈퇴/삭제된 경우 포함)
    PASSWORD_NOT_MATCHED(HttpStatus.UNAUTHORIZED, "PASSWORD_NOT_MATCHED", "기존 비밀번호가 일치하지 않습니다."), // 비밀번호 변경 시 기존 비밀번호 확인 실패
    PASSWORD_CONFIRM_NOT_MATCHED(HttpStatus.BAD_REQUEST, "PASSWORD_CONFIRM_NOT_MATCHED", "새 비밀번호와 확인이 일치하지 않습니다."), // 새 비밀번호 != 비밀번호 확인
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD_FORMAT", "비밀번호는 8자 이상이어야 합니다."),// 비밀번호 규칙 위반
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "NICKNAME_ALREADY_EXISTS", "이미 사용 중인 닉네임입니다."), // 닉네임 중복

    // 파일(이미지) 업로드 관련 에러코드
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "EMPTY_FILE", "업로드할 파일이 비어 있습니다."), // 파일이 없거나 빈 경우
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "INVALID_FILE_TYPE", "이미지 파일(jpg, jpeg, png, gif)만 업로드할 수 있습니다."), // 허용되지 않는 확장자
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_UPLOAD_FAILED", "파일 업로드에 실패했습니다."); // 저장 중 IO 오류 등

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
