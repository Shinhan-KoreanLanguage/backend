package com.daehanforeigner.capstone.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 공통적으로 사용하는 에러코드
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "잘못된 요청입니다."), // 형식·파라미터를 잘못된 요청값으로 요청한 경우
    INVALID_SORT_PROPERTY(HttpStatus.BAD_REQUEST, "INVALID_SORT_PROPERTY", "정렬할 수 없는 항목입니다."), // 존재하지 않는 필드로 정렬을 요청한 경우
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증되지 않은 사용자입니다."), // 토큰이 없는 상태 혹은 로그아웃 되어 있는 상태
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "권한이 없는 사용자입니다."), // 인증은 되었으나 권한이 없는 상태 ex) 일반 사용자가 관리자 페이지 접근하는 경우
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."), // 존재하지 않는 경로 접근
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."), // 예상하지 못한 서버 오류

    // 로컬 로그인 관련 에러코드
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 가입된 이메일입니다."), // 중복 된 이메일로 회원가입을 시도할 경우
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 일치하지 않습니다."), // 로컬 로그인에서 이메일 또는 비밀번호가 일치하지 않는 경우
    WITHDRAWN_USER(HttpStatus.FORBIDDEN, "WITHDRAWN_USER", "탈퇴한 사용자입니다."), // status = WITHDRAWN인 경우
    // 아래 두 개는 소프트 딜리트(14일) 때문에 탈퇴 회원의 행이 남아 있어 발생.
    // "이미 가입된 이메일"로 응답하면 본인이 탈퇴시킨 계정인 줄 몰라 프론트에서 안내가 불가능하므로 구분한다
    WITHDRAWN_EMAIL_NOT_REUSABLE(HttpStatus.CONFLICT, "WITHDRAWN_EMAIL_NOT_REUSABLE", "탈퇴한 계정의 이메일입니다. 탈퇴 후 14일이 지나면 다시 사용할 수 있습니다."), // 탈퇴 회원의 이메일로 재가입 시도
    WITHDRAWN_NICKNAME_NOT_REUSABLE(HttpStatus.CONFLICT, "WITHDRAWN_NICKNAME_NOT_REUSABLE", "탈퇴한 계정의 닉네임입니다. 탈퇴 후 14일이 지나면 다시 사용할 수 있습니다."), // 탈퇴 회원의 닉네임으로 재가입 시도

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
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "INVALID_FILE_TYPE", "허용되지 않는 파일 형식입니다."), // 허용되지 않는 파일 형식으로 업로드를 시도한 경우
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_UPLOAD_FAILED", "파일 업로드에 실패했습니다."), // 저장 중 IO 오류 등

    // 학습콘텐츠 관련 에러코드
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT_NOT_FOUND", "존재하지 않는 학습 콘텐츠입니다."), // 학습 콘텐츠 조회 실패
    CONTENT_IDS_REQUIRED(HttpStatus.BAD_REQUEST, "CONTENT_IDS_REQUIRED", "삭제할 학습 콘텐츠를 선택해주세요."), // 학습 콘텐츠 일괄 삭제 시 아무것도 선택하지 않거나 비어있을 때 삭제 요청을 시도한 경우
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "존재하지 않는 학습 카테고리입니다."), // 학습 카테고리 조회 실패
    MEDIA_FILE_REQUIRED(HttpStatus.BAD_REQUEST, "MEDIA_FILE_REQUIRED", "음성 파일과 영상 파일을 모두 등록해주세요."), // 콘텐츠 등록 시 미디어 누락
    DUPLICATE_TRANSLATION_LANGUAGE(HttpStatus.BAD_REQUEST, "DUPLICATE_TRANSLATION_LANGUAGE", "같은 언어의 번역을 두 번 등록할 수 없습니다."), // 번역 목록에 같은 language가 중복

    // AI 서버 관련 에러코드
    AI_SERVER_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "AI_SERVER_ERROR", "발음 분석 서버와 통신할 수 없습니다."), // 연결 실패·타임아웃·4xx/5xx 응답
    AI_REFERENCE_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_REFERENCE_NOT_FOUND", "원어민 기준 발음이 등록되지 않았습니다."), // AI 서버에 원어민 발음 기준이 없는 경우
    AI_MEDIA_ANALYSIS_FAILED(HttpStatus.BAD_REQUEST, "AI_MEDIA_ANALYSIS_FAILED", "업로드한 영상·음성을 분석할 수 없습니다."), // AI 서버가 422로 거부 — 원인은 얼굴 미검출·파일 형식·AI 서버 내부 오류 등 다양하므로 단정하지 않고 서버 로그로 확인한다
    ATTEMPT_NOT_FOUND(HttpStatus.NOT_FOUND, "ATTEMPT_NOT_FOUND", "존재하지 않는 발음 시도입니다."), // 발음 시도 조회 실패
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", "저장된 파일을 찾을 수 없습니다."), // 저장된 파일을 다시 읽으려 했으나 없는 경우

    // 발음 게임 관련 에러코드
    GAME_NOT_FOUND(HttpStatus.NOT_FOUND, "GAME_NOT_FOUND", "존재하지 않는 게임입니다."), // gameResultId로 게임 조회 실패
    GAME_ALREADY_FINISHED(HttpStatus.CONFLICT, "GAME_ALREADY_FINISHED", "이미 종료된 게임입니다."), // 종료된 게임에 단어를 제출하거나 다시 종료 요청한 경우
    NO_WORDS_AVAILABLE(HttpStatus.NOT_FOUND, "NO_WORDS_AVAILABLE", "해당 카테고리에 게임으로 사용할 단어가 없습니다."), // 카테고리에 WORD 타입 콘텐츠가 없는 경우

    // 통계 관련 에러코드
    INVALID_DATE_PARAMETER(HttpStatus.BAD_REQUEST, "INVALID_DATE_PARAMETER", "유효하지 않은 연도 또는 월입니다."); // year·month를 하나만 주었거나 달력상 존재하지 않는 월인 경우

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
