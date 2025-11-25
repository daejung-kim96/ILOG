package com.ssafy.user.application.event.dto;

import lombok.Getter;

@Getter
public class UserCreatedPayload {
    private Long id;
    private String email;
    private String name;
    private String picture;
}
