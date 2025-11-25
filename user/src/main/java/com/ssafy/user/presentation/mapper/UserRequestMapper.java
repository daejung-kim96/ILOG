package com.ssafy.user.presentation.mapper;

import org.springframework.stereotype.Component;

@Component
public class UserRequestMapper {
    public long toUserId(String userId) {
        return Long.parseLong(userId);
    }
}
