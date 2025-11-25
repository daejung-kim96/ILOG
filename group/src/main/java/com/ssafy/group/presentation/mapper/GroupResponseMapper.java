package com.ssafy.group.presentation.mapper;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ssafy.group.application.query.dto.GroupInfoModel;
import com.ssafy.group.application.query.dto.GroupMemberInfoModel;
import com.ssafy.group.domain.model.aggregate.Group;
import com.ssafy.group.presentation.rest.dto.response.GroupMemberResponse;
import com.ssafy.group.presentation.rest.dto.response.GroupResponse;

@Component
public class GroupResponseMapper {
    public GroupResponse toGroupResponse(Group group) {
        return GroupResponse.from(group);
    }

    public GroupResponse toGroupResponse(GroupInfoModel groupInfoModel) {
        if (groupInfoModel == null) {
            return null;
        }

        Set<GroupMemberResponse> memberResponses = (groupInfoModel.getGroupMembers() == null)
                ? Collections.emptySet()
                : groupInfoModel.getGroupMembers().stream()
                        .map(this::toGroupMemberResponse)
                        .collect(Collectors.toSet());

        return new GroupResponse(
                groupInfoModel.getId(),
                groupInfoModel.getName(),
                groupInfoModel.getInviteCode(),
                memberResponses,
                groupInfoModel.getCreatedAt(),
                groupInfoModel.getUpdatedAt());
    }

    private GroupMemberResponse toGroupMemberResponse(GroupMemberInfoModel groupMemberInfoModel) {
        if (groupMemberInfoModel == null) {
            return null;
        }
        return new GroupMemberResponse(
                groupMemberInfoModel.getId(),
                groupMemberInfoModel.getUserId(),
                groupMemberInfoModel.getUserName(),
                groupMemberInfoModel.getCreatedAt(),
                groupMemberInfoModel.getUpdatedAt());
    }
}
