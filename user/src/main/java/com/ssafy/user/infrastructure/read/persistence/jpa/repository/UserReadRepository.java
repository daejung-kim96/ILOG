package com.ssafy.user.infrastructure.read.persistence.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssafy.user.infrastructure.read.persistence.jpa.entity.UserReadEntity;
import com.ssafy.user.infrastructure.read.query.UserQueryRepository;

public interface UserReadRepository extends JpaRepository<UserReadEntity, Long>, UserQueryRepository {
}
