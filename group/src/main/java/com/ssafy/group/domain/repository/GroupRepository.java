package com.ssafy.group.domain.repository;

import java.util.Optional;

import com.ssafy.group.domain.model.aggregate.Group;

public interface GroupRepository {
    Group save(Group group);

    Optional<Group> findById(long groupId);

    Optional<Group> findByInviteCode(String inviteCode);

}