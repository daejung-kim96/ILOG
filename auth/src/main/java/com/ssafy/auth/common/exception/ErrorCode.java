package com.ssafy.auth.common.exception;

import org.springframework.http.HttpStatus;
import static org.springframework.http.HttpStatus.*;

public enum ErrorCode {

    // 공통 도메인 에러
    COMMON_INVALID_INPUT(BAD_REQUEST, "COMMON_INVALID_INPUT", "잘못된 입력값입니다."),
    COMMON_INVALID_TYPE(BAD_REQUEST, "COMMON_INVALID_TYPE", "잘못된 타입입니다."),
    COMMON_NOT_FOUND(NOT_FOUND, "COMMON_NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."),
    COMMON_METHOD_NOT_ALLOWED(METHOD_NOT_ALLOWED, "COMMON_METHOD_NOT_ALLOWED", "허용되지 않는 메서드입니다."),
    COMMON_INTERNAL_SERVER_ERROR(INTERNAL_SERVER_ERROR, "COMMON_INTERNAL_SERVER_ERROR", "내부 서버 오류입니다."),
    COMMON_INVALID_REQUEST(BAD_REQUEST, "COMMON_INVALID_REQUEST", "잘못된 요청입니다."),
    COMMON_ENTITY_NOT_FOUND(NOT_FOUND, "COMMON_ENTITY_NOT_FOUND", "요청한 엔티티를 찾을 수 없습니다."),

    // 인증 도메인 에러
    AUTH_USER_NOT_FOUND(NOT_FOUND, "AUTH_USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    AUTH_DUPLICATE_USER_EMAIL(CONFLICT, "AUTH_DUPLICATE_USER_EMAIL", "이미 존재하는 이메일입니다."),
    AUTH_FAIL_KEY_INIT(INTERNAL_SERVER_ERROR, "AUTH_FAIL_KEY_INIT", "JWT 키 초기화에 실패했습니다."),
    AUTH_UNAUTHORIZED(UNAUTHORIZED, "AUTH_UNAUTHORIZED", "인증이 필요합니다."),
    AUTH_INVALID_TOKEN(UNAUTHORIZED, "AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    AUTH_EXPIRED_TOKEN(UNAUTHORIZED, "AUTH_EXPIRED_TOKEN", "만료된 토큰입니다."),
    AUTH_ACCESS_DENIED(FORBIDDEN, "AUTH_ACCESS_DENIED", "접근이 거부되었습니다."),
    AUTH_FORBIDDEN_ACCESS(FORBIDDEN, "AUTH_FORBIDDEN_ACCESS", "접근 권한이 없습니다."),
    AUTH_UNAUTHENTICATED_USER(UNAUTHORIZED, "AUTH_UNAUTHENTICATED_USER", "인증되지 않은 사용자입니다."),
    AUTH_INVALID_USER_ROLE(BAD_REQUEST, "AUTH_INVALID_USER_ROLE", "유효하지 않은 사용자 역할입니다."),
    AUTH_INSUFFICIENT_PERMISSION(FORBIDDEN, "AUTH_INSUFFICIENT_PERMISSION", "권한이 부족합니다."),
    AUTH_TOKEN_MISSING(UNAUTHORIZED, "AUTH_TOKEN_MISSING", "인증 토큰이 필요합니다."),
    AUTH_INVALID_HEADER_FORMAT(UNAUTHORIZED, "AUTH_INVALID_HEADER_FORMAT", "Authorization 헤더 형식이 올바르지 않습니다."),
    AUTH_INVALID_SIGNATURE(UNAUTHORIZED, "AUTH_INVALID_SIGNATURE", "토큰 서명이 유효하지 않습니다."),
    AUTH_MALFORMED_TOKEN(UNAUTHORIZED, "AUTH_MALFORMED_TOKEN", "토큰 형식이 올바르지 않습니다."),
    AUTH_MISSING_REQUIRED_CLAIM(UNAUTHORIZED, "AUTH_MISSING_REQUIRED_CLAIM", "필수 토큰 정보가 누락되었습니다."),

    // OAuth2 도메인 에러
    AUTH_OAUTH2_AUTHENTICATION_FAILED(UNAUTHORIZED, "AUTH_OAUTH2_AUTHENTICATION_FAILED", "OAuth2 인증에 실패했습니다."),
    AUTH_OAUTH2_USER_INFO_FAILED(BAD_REQUEST, "AUTH_OAUTH2_USER_INFO_FAILED", "OAuth2 사용자 정보 처리에 실패했습니다."),
    AUTH_OAUTH2_UNSUPPORTED_PROVIDER(BAD_REQUEST, "AUTH_OAUTH2_UNSUPPORTED_PROVIDER", "지원하지 않는 OAuth2 제공자입니다."),
    AUTH_OAUTH2_USER_CREATION_FAILED(INTERNAL_SERVER_ERROR, "AUTH_OAUTH2_USER_CREATION_FAILED",
            "OAuth2 사용자 생성에 실패했습니다."),
    AUTH_OAUTH2_USER_UPDATE_FAILED(INTERNAL_SERVER_ERROR, "AUTH_OAUTH2_USER_UPDATE_FAILED",
            "OAuth2 사용자 정보 업데이트에 실패했습니다."),

    // OutBox 에러
    AUTH_OUTBOX_PAYLOAD_SERIALIZE_FAILED(INTERNAL_SERVER_ERROR, "AUTH_OUTBOX_PAYLOAD_SERIALIZE_FAILED",
            "Outbox 페이로드 직렬화에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}