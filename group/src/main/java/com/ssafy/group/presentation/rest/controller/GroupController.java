package com.ssafy.group.presentation.rest.controller;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.group.application.facade.GroupFacade;
import com.ssafy.group.common.response.ApiResponse;
import com.ssafy.group.presentation.mapper.GroupMapper;
import com.ssafy.group.presentation.rest.api.GroupApi;
import com.ssafy.group.presentation.rest.dto.request.CreateGroupRequest;
import com.ssafy.group.presentation.rest.dto.request.CreateGroupRequestBody;
import com.ssafy.group.presentation.rest.dto.request.InviteGroupRequestBody;
import com.ssafy.group.presentation.rest.dto.response.GroupResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
public class GroupController implements GroupApi {

    private final GroupFacade groupFacade;
    private final GroupMapper groupMapper;

    @PostMapping
    public ApiResponse<GroupResponse> createGroup(@RequestHeader("X-User-Id") String userId,
            @RequestBody CreateGroupRequestBody createGroupRequestBody) {
        GroupResponse groupResponse = groupMapper
                .toGroupResponse(groupFacade.createGroup(groupMapper.toCreateGroupRequest(userId,
                        createGroupRequestBody.name(), createGroupRequestBody.userName())));
        return ApiResponse.of(201, groupResponse);
    }

    @PostMapping("/invite")
    public ApiResponse<GroupResponse> inviteGroup(@RequestHeader("X-User-Id") String userId,
            @RequestBody InviteGroupRequestBody inviteGroupRequestBody) {
        GroupResponse groupResponse = groupMapper
                .toGroupResponse(groupFacade
                        .inviteGroup(groupMapper.toInviteGroupRequest(userId, inviteGroupRequestBody.inviteCode(),
                                inviteGroupRequestBody.userName())));
        return ApiResponse.of(200, groupResponse);
    }

    @Override
    public ApiResponse<Set<GroupResponse>> getGroup(@RequestHeader("X-User-Id") String userId) {
        Set<GroupResponse> groupResponses = groupFacade.getGroup(Long.parseLong(userId)).stream()
                .map(groupMapper::toGroupResponse)
                .collect(Collectors.toSet());
        return ApiResponse.of(200, groupResponses);
    }

}