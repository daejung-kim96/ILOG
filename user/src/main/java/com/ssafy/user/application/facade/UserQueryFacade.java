package com.ssafy.user.application.facade;

import org.springframework.stereotype.Component;

import com.ssafy.user.application.query.dto.UserInfoModel;
import com.ssafy.user.application.query.service.UserQueryService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserQueryFacade {

    private final UserQueryService userQueryService;

    public UserInfoModel getInfo(long userId) {
        return userQueryService.getInfo(userId);
    }

}
