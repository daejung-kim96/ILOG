package com.ssafy.user.infrastructure.read.persistence.jpa.mapper;

import org.springframework.stereotype.Component;

import com.ssafy.user.application.query.dto.UserInfoModel;
import com.ssafy.user.infrastructure.read.persistence.jpa.entity.UserReadEntity;

@Component
public class UserReadEntityMapper {

    public UserReadEntity toEntity(UserInfoModel userInfoModel) {
        return UserReadEntity.builder()
                .id(userInfoModel.getId())
                .email(userInfoModel.getEmail())
                .name(userInfoModel.getName())
                .picture(userInfoModel.getPicture())
                .createdAt(userInfoModel.getCreatedAt())
                .updatedAt(userInfoModel.getUpdatedAt())
                .build();
    }

    public UserInfoModel toModel(UserReadEntity userReadEntity) {
        if (userReadEntity == null) {
            return null;
        }
        return UserInfoModel.fromDb(
                userReadEntity.getId(),
                userReadEntity.getEmail(),
                userReadEntity.getPicture(),
                userReadEntity.getName(),
                userReadEntity.getCreatedAt(),
                userReadEntity.getUpdatedAt());
    }
}
