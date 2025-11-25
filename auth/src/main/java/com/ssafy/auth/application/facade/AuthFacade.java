package com.ssafy.auth.application.facade;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.auth.application.command.dto.TokenCookie;
import com.ssafy.auth.domain.model.aggregate.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthFacade {
    private final AuthCommandFacade authCommandFacade;
    private final AuthQueryFacade authQueryFacade;

    public TokenCookie reissueRefreshToken(String refreshToken) {
        return authCommandFacade.reissueRefreshToken(refreshToken);
    }

    @Transactional
    public User createUser(String email, String provider, String providerId, String name, String picture) {
        return authCommandFacade.createUser(email, provider, providerId, name, picture);
    }

    public void logout(long userId) {
        authCommandFacade.logout(userId);
    }

}
