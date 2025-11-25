package com.ssafy.auth.common.advice;

import com.ssafy.auth.common.response.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "com.ssafy.auth")
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 컨트롤러 메서드의 반환 타입이 ResponseEntity이면, 이 Advice를 적용하지 않음
        return !returnType.getParameterType().equals(ResponseEntity.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request, ServerHttpResponse response) {

        // 만약 body가 이미 ApiResponse 타입이라면, status 코드를 HTTP 응답에 반영
        if (body instanceof ApiResponse<?> apiResponse) {
            response.setStatusCode(HttpStatus.valueOf(apiResponse.status()));
            return body;
        }

        // ApiResponse나 ResponseEntity가 아닌 일반 객체는 ApiResponse.success로 감싸서 반환
        return ApiResponse.of(body);
    }
}
