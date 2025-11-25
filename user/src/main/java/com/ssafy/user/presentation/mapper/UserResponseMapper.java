package com.ssafy.user.presentation.mapper;

import org.springframework.stereotype.Component;

import com.ssafy.user.application.query.dto.UserInfoModel;
import com.ssafy.user.presentation.rest.dto.response.UserInfoResponse;

@Component
public class UserResponseMapper {

    public UserInfoResponse toUserInfoResponse(UserInfoModel userInfoModel) {
        return UserInfoResponse.builder()
                .id(userInfoModel.getId())
                .email(userInfoModel.getEmail())
                .name(userInfoModel.getName())
                .picture(userInfoModel.getPicture())
                .createdAt(userInfoModel.getCreatedAt().toString())
                .updatedAt(userInfoModel.getUpdatedAt() != null ? userInfoModel.getUpdatedAt().toString() : null)
                .build();
    }

}
