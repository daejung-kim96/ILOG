package com.ssafy.group.infrastructure.read.persistence.jpa.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ssafy.group.application.query.dto.GroupInfoModel;
import com.ssafy.group.application.query.dto.GroupMemberInfoModel;
import com.ssafy.group.infrastructure.read.persistence.jpa.entity.GroupMemberReadEntity;
import com.ssafy.group.infrastructure.read.persistence.jpa.entity.GroupReadEntity;

@Component
public class GroupInfoModelMapper {

    public GroupInfoModel toGroupInfoModel(GroupReadEntity groupReadEntity) {
        if (groupReadEntity == null) {
            return null;
        }

        GroupInfoModel groupInfoModel = new GroupInfoModel(
                groupReadEntity.getId(),
                groupReadEntity.getName(),
                groupReadEntity.getInviteCode(),
                null, // groupMembers will be set below
                groupReadEntity.getCreatedAt(),
                groupReadEntity.getUpdatedAt());

        if (groupReadEntity.getGroupMembers() != null) {
            groupInfoModel.setGroupMembers(groupReadEntity.getGroupMembers().stream()
                    .map(this::toGroupMemberInfoModel)
                    .collect(Collectors.toSet()));
        }

        return groupInfoModel;
    }

    public GroupMemberInfoModel toGroupMemberInfoModel(GroupMemberReadEntity groupMemberReadEntity) {
        if (groupMemberReadEntity == null) {
            return null;
        }

        return new GroupMemberInfoModel(
                groupMemberReadEntity.getId(),
                groupMemberReadEntity.getUserId(),
                groupMemberReadEntity.getUserName(),
                groupMemberReadEntity.getCreatedAt(),
                groupMemberReadEntity.getUpdatedAt());
    }
}
