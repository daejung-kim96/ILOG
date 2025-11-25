package com.ssafy.auth.application.facade;

import org.springframework.stereotype.Component;

import com.ssafy.auth.application.command.TokenCommand;
import com.ssafy.auth.application.command.UserCommand;
import com.ssafy.auth.application.command.dto.TokenCookie;
import com.ssafy.auth.domain.model.aggregate.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthCommandFacade {

    public final TokenCommand tokenCommand;
    public final UserCommand userCommand;

    public TokenCookie reissueRefreshToken(String refreshToken) {
        return tokenCommand.reissueRefreshToken(refreshToken);
    }

    public User createUser(String email, String provider, String providerId, String name, String picture) {
        return userCommand.createUser(email, provider, providerId, name, picture);
    }

    public void logout(long userId) {
        tokenCommand.logout(userId);
    }
}
