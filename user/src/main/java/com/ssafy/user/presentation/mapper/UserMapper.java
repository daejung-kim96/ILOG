package com.ssafy.user.presentation.mapper;

import org.springframework.stereotype.Component;

import com.ssafy.user.application.query.dto.UserInfoModel;
import com.ssafy.user.presentation.rest.dto.response.UserInfoResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final UserRequestMapper userRequestMapper;
    private final UserResponseMapper userResponseMapper;

    public Long toUserId(String userId) {
        return userRequestMapper.toUserId(userId);
    }

    public UserInfoResponse toUserInfoResponse(UserInfoModel userInfoModel) {
        return userResponseMapper.toUserInfoResponse(userInfoModel);
    }
}
