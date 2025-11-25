package com.ssafy.user.presentation.rest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.user.application.facade.UserFacade;
import com.ssafy.user.common.response.ApiResponse;
import com.ssafy.user.presentation.mapper.UserMapper;
import com.ssafy.user.presentation.rest.api.UserApi;
import com.ssafy.user.presentation.rest.dto.response.UserInfoResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController implements UserApi {

    private final UserFacade userFacade;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> getMyInfo(@RequestHeader("X-User-Id") String userId) {

        return ApiResponse.of(200, userMapper.toUserInfoResponse(userFacade.getInfo(userMapper.toUserId(userId))));
    }

}