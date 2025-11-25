package com.ssafy.group.application.command.service;

import org.springframework.stereotype.Service;

import com.ssafy.group.common.exception.BusinessException;
import com.ssafy.group.common.exception.ErrorCode;
import com.ssafy.group.domain.model.aggregate.Group;
import com.ssafy.group.domain.repository.GroupRepository;
import com.ssafy.group.infrastructure.read.persistence.jpa.entity.GroupMemberReadEntity;
import com.ssafy.group.infrastructure.read.persistence.jpa.entity.GroupReadEntity;
import com.ssafy.group.infrastructure.read.persistence.jpa.repository.GroupReadRepository;
import com.ssafy.group.presentation.rest.dto.request.CreateGroupRequest;
import com.ssafy.group.presentation.rest.dto.request.InviteGroupRequest;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupCommandService {
        private final GroupRepository groupRepository;
        private final GroupReadRepository groupReadRepository;

        @Transactional
        public Group createGroup(CreateGroupRequest createGroupRequest) {
                Group newGroup = Group.createGroup(createGroupRequest.userId(),
                                createGroupRequest.name(),
                                createGroupRequest.userName());
                Group savedGroup = groupRepository.save(newGroup);
                GroupReadEntity groupReadEntity = GroupReadEntity.builder().id(savedGroup.getId())
                                .name(savedGroup.getName())
                                .inviteCode(savedGroup.getInviteCode())
                                .createdAt(savedGroup.getCreatedAt())
                                .updatedAt(savedGroup.getUpdatedAt())
                                .build();

                savedGroup.getGroupMembers().forEach(member -> {
                        GroupMemberReadEntity memberReadEntity = GroupMemberReadEntity.builder()
                                        .id(member.getId())
                                        .userId(member.getUserId())
                                        .userName(member.getUserName())
                                        .group(groupReadEntity)
                                        .createdAt(member.getCreatedAt())
                                        .updatedAt(member.getUpdatedAt())
                                        .build();
                        groupReadEntity.addGroupMember(memberReadEntity);
                });

                groupReadRepository.save(groupReadEntity);
                return savedGroup;
        }

        @Transactional
        public Group inviteGroup(InviteGroupRequest inviteGroupRequest) {
                Group group = groupRepository.findByInviteCode(inviteGroupRequest.inviteCode())
                                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_GROUP_NOT_FOUND));
                group.addMember(inviteGroupRequest.userId(), inviteGroupRequest.userName());
                Group savedGroup = groupRepository.save(group);

                GroupReadEntity groupReadEntity = groupReadRepository.findById(savedGroup.getId())
                                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_READ_GROUP_NOT_FOUND));

                groupReadEntity.getGroupMembers().clear();
                savedGroup.getGroupMembers().forEach(member -> {
                        GroupMemberReadEntity memberReadEntity = GroupMemberReadEntity.builder()
                                        .id(member.getId())
                                        .userId(member.getUserId())
                                        .userName(member.getUserName())
                                        .group(groupReadEntity)
                                        .createdAt(member.getCreatedAt())
                                        .updatedAt(member.getUpdatedAt())
                                        .build();
                        groupReadEntity.addGroupMember(memberReadEntity);
                });

                groupReadRepository.save(groupReadEntity);
                return savedGroup;
        }

}
