package com.ssafy.group.presentation.mapper;

import org.springframework.stereotype.Component;

import com.ssafy.group.application.query.dto.GroupInfoModel;
import com.ssafy.group.domain.model.aggregate.Group;
import com.ssafy.group.presentation.rest.dto.request.CreateGroupRequest;
import com.ssafy.group.presentation.rest.dto.request.InviteGroupRequest;
import com.ssafy.group.presentation.rest.dto.response.GroupResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GroupMapper {
    private final GroupRequestMapper groupRequestMapper;
    private final GroupResponseMapper groupResponseMapper;

    public CreateGroupRequest toCreateGroupRequest(String userId, String name, String userName) {
        return groupRequestMapper.toCreateGroupRequest(userId, name, userName);

    }

    public InviteGroupRequest toInviteGroupRequest(String userId, String inviteCode, String userName) {
        return groupRequestMapper.toInviteGroupRequest(userId, inviteCode, userName);
    }

    public GroupResponse toGroupResponse(Group group) {
        return groupResponseMapper.toGroupResponse(group);
    }

    public GroupResponse toGroupResponse(GroupInfoModel groupInfoModel) {
        return groupResponseMapper.toGroupResponse(groupInfoModel);
    }
}
