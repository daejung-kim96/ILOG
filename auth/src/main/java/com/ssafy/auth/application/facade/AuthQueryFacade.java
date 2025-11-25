package com.ssafy.auth.application.facade;

import org.springframework.stereotype.Component;

import com.ssafy.auth.application.query.UserQuery;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthQueryFacade {
    private final UserQuery userQuery;

}
