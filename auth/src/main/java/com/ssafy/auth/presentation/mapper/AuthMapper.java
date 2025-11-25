package com.ssafy.auth.presentation.mapper;

import org.springframework.stereotype.Component;

import com.ssafy.auth.application.command.dto.TokenCookie;
import com.ssafy.auth.presentation.rest.dto.response.TokenResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthMapper {
    private final AuthRequestMapper authRequestMapper;
    private final AuthResponseMapper authResponseMapper;

    public TokenResponse toTokenResponse(TokenCookie tokenCookie) {
        return authResponseMapper.toTokenResponse(tokenCookie);
    }
}
