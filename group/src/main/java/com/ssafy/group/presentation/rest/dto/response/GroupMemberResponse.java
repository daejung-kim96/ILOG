package com.ssafy.group.presentation.rest.dto.response;

import java.time.LocalDateTime;

import com.ssafy.group.domain.model.entity.GroupMember;

public record GroupMemberResponse(
        long id,
        long userId,
        String userName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static GroupMemberResponse from(GroupMember member) {
        if (member == null) {
            return null;
        }

        return new GroupMemberResponse(
                member.getId(),
                member.getUserId(),
                member.getUserName(),
                member.getCreatedAt(),
                member.getUpdatedAt());
    }
}