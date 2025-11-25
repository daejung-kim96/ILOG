package com.ssafy.group.infrastructure.read.persistence.jpa.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssafy.group.infrastructure.read.persistence.jpa.entity.GroupReadEntity;

public interface GroupReadRepository extends JpaRepository<GroupReadEntity, Long> {
    Set<GroupReadEntity> findByGroupMembers_UserId(long userId);
}
