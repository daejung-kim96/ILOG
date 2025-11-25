package com.ssafy.group.infrastructure.write.persistence.jpa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssafy.group.infrastructure.write.persistence.jpa.entity.GroupEntity;

public interface GroupJpaRepository extends JpaRepository<GroupEntity, Long> {
    Optional<GroupEntity> findByInviteCode(String inviteCode);
}
