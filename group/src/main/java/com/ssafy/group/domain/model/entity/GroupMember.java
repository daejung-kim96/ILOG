package com.ssafy.group.domain.model.entity;

import java.time.LocalDateTime;

import com.ssafy.group.domain.model.aggregate.Group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GroupMember {

    public GroupMember(Group group, long userId, String userName) {
        this.group = group;
        this.userId = userId;
        this.userName = userName;
    }

    public static GroupMember fromDb(
            long id,
            long userId,
            String userName,
            Group group,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new GroupMember(id, userId, userName, group, createdAt, updatedAt);
    }

    long id;
    long userId;
    String userName;
    public Group group;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
