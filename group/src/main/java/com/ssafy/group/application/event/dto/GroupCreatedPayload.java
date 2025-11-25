package com.ssafy.group.application.event.dto;

import lombok.Getter;

@Getter
public class GroupCreatedPayload {
    private Long id;
    private String email;
    private String name;
    private String picture;
}
