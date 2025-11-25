package com.ssafy.user.infrastructure.write.persistence.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssafy.user.infrastructure.write.persistence.jpa.entity.UserEntity;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

}
