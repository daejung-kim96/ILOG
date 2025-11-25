package com.ssafy.group.presentation.mapper;

import org.springframework.stereotype.Component;

import com.ssafy.group.presentation.rest.dto.request.CreateGroupRequest;
import com.ssafy.group.presentation.rest.dto.request.InviteGroupRequest;

@Component
public class GroupRequestMapper {
    public CreateGroupRequest toCreateGroupRequest(String userId, String name, String userName) {
        return CreateGroupRequest.of(Long.parseLong(userId), name, userName);
    }

    public InviteGroupRequest toInviteGroupRequest(String userId, String inviteCode, String userName) {
        return InviteGroupRequest.of(Long.parseLong(userId), inviteCode, userName);
    }
}
