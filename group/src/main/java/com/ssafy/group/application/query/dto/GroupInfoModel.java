package com.ssafy.group.application.query.dto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupInfoModel {
    private Long id;
    private String name;
    private String inviteCode;
    private Set<GroupMemberInfoModel> groupMembers = new HashSet<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
