package com.ssafy.group.presentation.rest.dto.request;

import lombok.Builder;

@Builder
public record InviteGroupRequest(
        long userId,
        String inviteCode,
        String userName) {
    public static InviteGroupRequest of(long userId, String inviteCode, String name) {
        return new InviteGroupRequest(userId, inviteCode, name);
    }
}
