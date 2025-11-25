package com.ssafy.group.presentation.rest.dto.response;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import com.ssafy.group.domain.model.aggregate.Group;

public record GroupResponse(
                long id,
                String name,
                String inviteCode,
                Set<GroupMemberResponse> members,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {

        public static GroupResponse from(Group group) {
                if (group == null) {
                        return null;
                }

                Set<GroupMemberResponse> memberResponses = (group.getGroupMembers() == null)
                                ? Collections.emptySet()
                                : group.getGroupMembers().stream()
                                                .map(GroupMemberResponse::from)
                                                .collect(Collectors.toSet());
                return new GroupResponse(
                                group.getId(),
                                group.getName(),
                                group.getInviteCode(),
                                memberResponses,
                                group.getCreatedAt(),
                                group.getUpdatedAt());
        }
}
