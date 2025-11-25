package com.ssafy.auth.application.command;

import org.springframework.stereotype.Component;

import com.ssafy.auth.application.command.dto.TokenCookie;
import com.ssafy.auth.common.exception.BusinessException;
import com.ssafy.auth.common.exception.ErrorCode;
import com.ssafy.auth.infrastructure.jwt.JwtUtil;
import com.ssafy.auth.infrastructure.service.RedisService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenCommand {

    private final JwtUtil jwtUtil;
    private final RedisService redisService;

    public TokenCookie reissueRefreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
        }

        long userId = jwtUtil.getUserIdFromToken(refreshToken);

        if (!redisService.validateRefreshToken(userId, refreshToken)) {
            // TODO : 보안강화
            // (보안 강화) 만약 서명은 유효한데 Redis에 없는 토큰(로그아웃/탈취됨)이 들어오면
            // 탈취 시도로 간주하고 해당 유저의 모든 RT를 삭제(redisService.deleteRefreshToken(userId))하는 로직을
            // 여기에 추가할 수 있습니다. (OAuth2.0 BCP 권장)
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
        }

        String newAccessToken = jwtUtil.createAccessToken(userId);
        String newRefreshToken = jwtUtil.createRefreshToken(userId);

        redisService.saveRefreshToken(userId, newRefreshToken);

        return TokenCookie.of(newAccessToken, newRefreshToken);
    }

    public void logout(long userId) {
        redisService.deleteRefreshToken(userId);
    }
}
