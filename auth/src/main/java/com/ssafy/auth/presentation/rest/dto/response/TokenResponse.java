package com.ssafy.auth.presentation.rest.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken) {
    public static TokenResponse of(String accessToken, String refreshToken) {
        TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken);
        return tokenResponse;
    }
}
