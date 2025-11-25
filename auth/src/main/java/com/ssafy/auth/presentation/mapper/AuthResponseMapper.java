package com.ssafy.auth.presentation.mapper;

import org.springframework.stereotype.Component;

import com.ssafy.auth.application.command.dto.TokenCookie;
import com.ssafy.auth.presentation.rest.dto.response.TokenResponse;

@Component
public class AuthResponseMapper {

    TokenResponse toTokenResponse(TokenCookie tokenCookie) {
        String accessToken = tokenCookie.accessToken();
        String refreshToken = tokenCookie.refreshToken();
        return TokenResponse.of(accessToken, refreshToken);
    }

}
