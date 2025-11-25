package ilog.back.common.exception;

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

    // 사용자 도메인 에러
    USER_KAFKA_EVENT_ERROR(INTERNAL_SERVER_ERROR, "USER_KAFKA_EVENT_ERROR", "카프카 이벤트 처리 중 오류가 발생했습니다."),
    USER_OUTBOX_PAYLOAD_UNSERIALIZE_FAILED(BAD_REQUEST, "USER_OUTBOX_PAYLOAD_UNSERIALIZE_FAILED",
            "아웃박스 페이로드 역직렬화에 실패했습니다."),
    USER_REGISTER_TYPE_ERROR(BAD_REQUEST, "USER_REGISTER_TYPE_ERROR", "사용자 등록 타입 오류입니다."),
    USER_NOT_FOUND(NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    USER_DUPLICATE_EMAIL(CONFLICT, "USER_DUPLICATE_EMAIL", "이미 존재하는 이메일입니다."),
    USER_INVALID_PASSWORD(BAD_REQUEST, "USER_INVALID_PASSWORD", "비밀번호가 올바르지 않습니다."),
    DUPLICATE_PHONE_NUMBER(CONFLICT, "DUPLICATE_PHONE_NUMBER", "이미 존재하는 전화번호입니다.");

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