package com.ssafy.user.application.facade;

import org.springframework.stereotype.Component;

import com.ssafy.user.application.command.service.UserCommandService;
import com.ssafy.user.application.event.dto.UserCreatedPayload;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserCommandFacade {
    private final UserCommandService userCommandService;

    public void createUser(UserCreatedPayload payloadDto) {
        userCommandService.createUser(payloadDto);
    }

}
