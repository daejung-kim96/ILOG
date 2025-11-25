package com.ssafy.group.application.facade;

import org.springframework.stereotype.Component;

import com.ssafy.group.application.command.service.GroupCommandService;
import com.ssafy.group.domain.model.aggregate.Group;
import com.ssafy.group.presentation.rest.dto.request.CreateGroupRequest;
import com.ssafy.group.presentation.rest.dto.request.InviteGroupRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GroupCommandFacade {
    private final GroupCommandService groupCommandService;

    public Group createGroup(CreateGroupRequest createGroupRequest) {
        return groupCommandService.createGroup(createGroupRequest);
    }

    public Group inviteGroup(InviteGroupRequest inviteGroupRequest) {
        return groupCommandService.inviteGroup(inviteGroupRequest);
    }
}
