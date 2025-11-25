package com.ssafy.gateway.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ✅ 성공 (Success)
    SUCCESS(2000, "요청에 성공했습니다.", HttpStatus.OK),

    // ❌ 실패: 클라이언트 오류 (Client Error)
    INVALID_REQUEST(4000, "유효하지 않은 요청입니다.", HttpStatus.BAD_REQUEST),
    
    UNAUTHORIZED(4010, "인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(4011, "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_NOT_FOUND(4012, "토큰을 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(4030, "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // ❌ 실패: 서버 오류 (Server Error)
    FAIL_KEY_INIT(5001, "암호화 키 초기화에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INTERNAL_SERVER_ERROR(5000, "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
