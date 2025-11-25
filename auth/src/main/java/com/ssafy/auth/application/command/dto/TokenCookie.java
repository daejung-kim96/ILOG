package com.ssafy.auth.application.command.dto;

import lombok.Builder;

@Builder
public record TokenCookie(String accessToken, String refreshToken) {
    public static TokenCookie of(String accessToken, String refreshToken) {
        TokenCookie tokenCookie = new TokenCookie(accessToken, refreshToken);
        return tokenCookie;
    }
}
