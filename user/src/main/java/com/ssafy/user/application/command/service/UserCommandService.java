package com.ssafy.user.application.command.service;

import org.springframework.stereotype.Service;

import com.ssafy.user.application.event.dto.UserCreatedPayload;
import com.ssafy.user.domain.model.aggregate.User;
import com.ssafy.user.domain.repository.UserRepository;
import com.ssafy.user.infrastructure.read.persistence.jpa.entity.UserReadEntity;
import com.ssafy.user.infrastructure.read.persistence.jpa.repository.UserReadRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserCommandService {
    private final UserRepository userRepository;
    private final UserReadRepository userReadRepository;

    @Transactional
    public void createUser(UserCreatedPayload payloadDto) {
        User newUser = User.createUser(payloadDto.getId(),
                payloadDto.getEmail(),
                payloadDto.getPicture(),
                payloadDto.getName());
        User savedUser = userRepository.save(newUser);
        UserReadEntity userReadEntity = UserReadEntity.builder().id(savedUser.getId().value())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .picture(savedUser.getPicture())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();
        userReadRepository.save(userReadEntity);
        // TODO : 읽기 DB의 원자성은 보장되지 않는다. 이후 batch 나 보상 로직으로 장기적 원자성이라도 보장해야 한다.
    }
}
