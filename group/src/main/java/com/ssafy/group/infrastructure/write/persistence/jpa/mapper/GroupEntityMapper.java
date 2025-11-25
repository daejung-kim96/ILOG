package com.ssafy.group.infrastructure.write.persistence.jpa.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ssafy.group.domain.model.aggregate.Group;
import com.ssafy.group.domain.model.entity.GroupMember;
import com.ssafy.group.infrastructure.write.persistence.jpa.entity.GroupEntity;
import com.ssafy.group.infrastructure.write.persistence.jpa.entity.GroupMemberEntity;

@Component
public class GroupEntityMapper {
    public void updateJpaEntity(Group group, GroupEntity groupEntity) {
        groupEntity.getGroupMembers().clear();
        group.getGroupMembers().forEach(groupMember -> {
            GroupMemberEntity groupMemberEntity = toJpaEntity(groupMember, groupEntity);
            groupEntity.addGroupMember(groupMemberEntity);
        });
    }

    public GroupEntity toJpaEntity(Group group) {
        GroupEntity groupEntity = GroupEntity.builder()
                .name(group.getName())
                .inviteCode(group.getInviteCode())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();

        group.getGroupMembers().forEach(groupMember -> {
            GroupMemberEntity groupMemberEntity = toJpaEntity(groupMember, groupEntity);
            groupEntity.addGroupMember(groupMemberEntity);
        });

        return groupEntity;
    }

    public GroupMemberEntity toJpaEntity(GroupMember groupMember, GroupEntity groupEntity) {
        return GroupMemberEntity.builder()
                .userId(groupMember.getUserId())
                .group(groupEntity)
                .userName(groupMember.getUserName())
                .createdAt(groupMember.getCreatedAt())
                .updatedAt(groupMember.getUpdatedAt())
                .build();
    }

    public Group toDomain(GroupEntity groupEntity) {
        Group group = Group.fromDb(
                groupEntity.getId(),
                groupEntity.getName(),
                groupEntity.getInviteCode(),
                groupEntity.getGroupMembers().stream()
                        .map(this::toDomain)
                        .collect(Collectors.toSet()),
                groupEntity.getCreatedAt(),
                groupEntity.getUpdatedAt());
        return group;
    }

    public GroupMember toDomain(GroupMemberEntity groupMemberEntity) {
        return GroupMember.fromDb(
                groupMemberEntity.getId(),
                groupMemberEntity.getUserId(),
                groupMemberEntity.getUserName(),
                null, // 순환 참조를 피하기 위해 여기서는 group을 null로 설정
                groupMemberEntity.getCreatedAt(),
                groupMemberEntity.getUpdatedAt());
    }
}
