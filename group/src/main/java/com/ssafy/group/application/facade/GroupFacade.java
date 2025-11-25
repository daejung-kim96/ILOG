package com.ssafy.group.application.facade;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.ssafy.group.application.query.dto.GroupInfoModel;
import com.ssafy.group.domain.model.aggregate.Group;
import com.ssafy.group.presentation.rest.dto.request.CreateGroupRequest;
import com.ssafy.group.presentation.rest.dto.request.InviteGroupRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GroupFacade {
    private final GroupCommandFacade groupCommandFacade;
    private final GroupQueryFacade groupQueryFacade;

    public Group createGroup(CreateGroupRequest createGroupRequest) {
        return groupCommandFacade.createGroup(createGroupRequest);
    }

    public GroupInfoModel getInfo(long groupId) {
        return groupQueryFacade.getInfo(groupId);
    }

    public Group inviteGroup(InviteGroupRequest inviteGroupRequest) {
        return groupCommandFacade.inviteGroup(inviteGroupRequest);
    }

    public Set<GroupInfoModel> getGroup(long userId) {
        return groupQueryFacade.getGroup(userId);
    }
}
