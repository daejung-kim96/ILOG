package com.ssafy.auth.infrastructure.write.persistence.jpa.mapper;

import com.ssafy.auth.domain.model.aggregate.User;
import com.ssafy.auth.infrastructure.write.persistence.jpa.entity.UserEntity;

import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper {

    public UserEntity toEntity(User user) {
        return UserEntity.builder()
                .id(user.getId().value())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .build();
    }

    public User toDomain(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }
        return User.fromDb(
                userEntity.getId(),
                userEntity.getProvider(),
                userEntity.getProviderId());
    }
}
