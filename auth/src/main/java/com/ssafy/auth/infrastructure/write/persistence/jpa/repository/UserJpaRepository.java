package com.ssafy.auth.infrastructure.write.persistence.jpa.repository;

import com.ssafy.auth.domain.model.enums.Provider;
import com.ssafy.auth.infrastructure.write.persistence.jpa.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByProviderAndProviderId(Provider provider, String providerId);

    boolean existsByProviderAndProviderId(Provider provider, String providerId);
}
