package com.ssafy.gateway.filter;

import com.ssafy.gateway.common.error.BusinessException;
import com.ssafy.gateway.common.error.ErrorCode;
import com.ssafy.gateway.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationHeaderFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final FilterAuthProperties filterAuthProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        if (isPermitAll(path)) {
            return chain.filter(exchange);
        }

        String token = getAccessToken(exchange);
        Claims claims = jwtUtil.getClaims(token);
        String userId = claims.getSubject();

        ServerHttpRequest newRequest = exchange.getRequest().mutate()
                .header("X-User-Id", userId)
                .build();

        return chain.filter(exchange.mutate().request(newRequest).build());
    }

    private boolean isPermitAll(String path) {
        return filterAuthProperties.permitAllPaths().stream()
                .anyMatch(pattern -> new AntPathMatcher().match(pattern, path));
    }

    private String getAccessToken(ServerWebExchange exchange) {
        HttpCookie cookie = exchange.getRequest()
                .getCookies()
                .getFirst("access_token");

        if (cookie == null) {
            throw new BusinessException(ErrorCode.TOKEN_NOT_FOUND);
        }

        return cookie.getValue();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
