package com.ssafy.auth.infrastructure.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    @Value("${jwt.refresh-token-validity-in-seconds}")
    private Long rtExpireSeconds;

    public void saveRefreshToken(long userId, String refreshToken) {
        // 1. Key 정의 (예: "RT:12345")
        String key = "RT:" + userId;

        // 2. opsForValue() : String 타입의 Key-Value를 다루는 연산자
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        // 3. set(Key, Value, Duration) : 만료 시간을 함께 설정
        ops.set(key, refreshToken, Duration.ofSeconds(rtExpireSeconds));
    }

    public boolean validateRefreshToken(long userId, String refreshTokenFromCookie) {
        String key = "RT:" + userId;
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        // 1. Redis에서 Key로 저장된 RT를 가져옵니다.
        String storedRefreshToken = ops.get(key);

        // 2. (중요) Redis에 토큰이 존재하고, 쿠키의 토큰과 일치하는지 확인
        return storedRefreshToken != null && storedRefreshToken.equals(refreshTokenFromCookie);
    }

    /**
     * RT 삭제 (로그아웃 시)
     */
    public void deleteRefreshToken(long userId) {
        String key = "RT:" + userId;

        // 3. delete(Key) : 키에 해당하는 K-V 삭제
        redisTemplate.delete(key);
    }
}