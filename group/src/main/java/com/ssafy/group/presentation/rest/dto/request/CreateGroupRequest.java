package com.ssafy.group.presentation.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateGroupRequest(
        @NotNull long userId,
        @NotNull String name,
        String userName) {
    public static CreateGroupRequest of(long userId, String name, String userName) {
        return new CreateGroupRequest(userId, name, userName);
    }
}
