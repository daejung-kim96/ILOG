package com.ssafy.user.application.facade;

import org.springframework.stereotype.Component;

import com.ssafy.user.application.event.dto.UserCreatedPayload;
import com.ssafy.user.application.query.dto.UserInfoModel;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserFacade {
    private final UserCommandFacade userCommandFacade;
    private final UserQueryFacade userQueryFacade;

    public UserInfoModel getInfo(long userId) {
        return userQueryFacade.getInfo(userId);
    }

    public void createUser(UserCreatedPayload payloadDto) {
        userCommandFacade.createUser(payloadDto);
    }
}
