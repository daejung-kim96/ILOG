package com.ssafy.group.domain.model.aggregate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;

import com.ssafy.group.common.exception.BusinessException;
import com.ssafy.group.common.exception.ErrorCode;
import com.ssafy.group.domain.model.entity.GroupMember;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Group {

    public static Group createGroup(long userId, String name, String userName) {
        Group group = new Group();
        group.name = name;
        group.inviteCode = RandomStringUtils.randomAlphanumeric(10);

        GroupMember creatorAsMember = new GroupMember(
                group,
                userId,
                userName);

        group.groupMembers.add(creatorAsMember);

        return group;
    }

    public static Group fromDb(
            long id,
            String name,
            String inviteCode,
            Set<GroupMember> groupMembers,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        Group group = new Group(id, name, new HashSet<>(), inviteCode, createdAt, updatedAt);
        group.setGroupMembers(groupMembers.stream().peek(gm -> gm.group = group).collect(Collectors.toSet()));
        return group;
    }

    public void addMember(long userId, String userName) {
        boolean alreadyMember = this.groupMembers.stream().anyMatch(gm -> gm.getUserId() == userId);
        if (alreadyMember) {
            throw new BusinessException(ErrorCode.GROUP_ALREADY_MEMBER);
        }
        GroupMember newMember = new GroupMember(this, userId, userName);
        this.groupMembers.add(newMember);
    }

    private long id;
    private String name;
    @Setter
    private Set<GroupMember> groupMembers = new HashSet<>();
    private String inviteCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
