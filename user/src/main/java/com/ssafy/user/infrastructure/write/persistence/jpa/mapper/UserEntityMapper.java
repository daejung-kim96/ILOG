package com.ssafy.user.infrastructure.write.persistence.jpa.mapper;

import org.springframework.stereotype.Component;

import com.ssafy.user.domain.model.aggregate.User;
import com.ssafy.user.infrastructure.write.persistence.jpa.entity.UserEntity;

@Component
public class UserEntityMapper {

    public UserEntity toEntity(User user) {
        return UserEntity.builder()
                .id(user.getId().value())
                .email(user.getEmail())
                .name(user.getName())
                .picture(user.getPicture())
                .build();
    }

    public User toDomain(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }
        return User.fromDb(
                userEntity.getId(),
                userEntity.getEmail(),
                userEntity.getPicture(),
                userEntity.getName(),
                userEntity.getCreatedAt(),
                userEntity.getUpdatedAt());
    }
}
