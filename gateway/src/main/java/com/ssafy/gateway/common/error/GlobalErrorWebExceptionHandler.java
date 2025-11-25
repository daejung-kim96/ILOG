package com.ssafy.gateway.common.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Order(-1) // 1. Spring의 기본 ErrorWebExceptionHandler보다 우선순위를 높게 설정
@Component
@RequiredArgsConstructor
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper; // 2. 객체를 JSON으로 직렬화하기 위해 ObjectMapper 주입

    @Override
    public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
        log.error("Global Exception Handler: {}", ex.getMessage(), ex);

        ErrorCode errorCode;
        switch (ex) {
            case BusinessException businessException -> errorCode = businessException.getErrorCode();
            case ResponseStatusException responseStatusException -> {
                // Spring Security 등에서 발생하는 예외 처리
                HttpStatus status = (HttpStatus) responseStatusException.getStatusCode();
                if (status.equals(HttpStatus.UNAUTHORIZED) || status.equals(HttpStatus.FORBIDDEN)) {
                    errorCode = ErrorCode.UNAUTHORIZED;
                } else {
                    errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
                }
            }
            default -> errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        }

        ErrorResponse errorResponse = new ErrorResponse(errorCode);
        exchange.getResponse().setStatusCode(errorCode.getHttpStatus());
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 4. ErrorResponse를 JSON으로 변환하여 응답 본문에 작성 (Reactive 스타일)
        Mono<DataBuffer> responseBody = Mono.defer(() ->
                Mono.fromCallable(() -> objectMapper.writeValueAsBytes(errorResponse))
                        .map(bytes -> exchange.getResponse().bufferFactory().wrap(bytes))
                        .doOnError(e -> log.error("Error writing JSON response", e))
                        .onErrorResume(e -> Mono.just(exchange.getResponse().bufferFactory().wrap(new byte[0])))
        );

        return exchange.getResponse().writeWith(responseBody).then();
    }
}
