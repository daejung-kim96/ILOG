package com.ssafy.auth.infrastructure.write.persistence.jpa.repository;

import com.ssafy.auth.domain.model.aggregate.User;
import com.ssafy.auth.domain.model.enums.Provider;
import com.ssafy.auth.domain.repository.UserRepository;
import com.ssafy.auth.infrastructure.write.persistence.jpa.entity.UserEntity;
import com.ssafy.auth.infrastructure.write.persistence.jpa.mapper.UserEntityMapper;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserEntityMapper userEntityMapper;

    @Override
    public User save(User user) {
        UserEntity userEntity = userEntityMapper.toEntity(user);
        UserEntity savedEntity = userJpaRepository.save(userEntity);
        return userEntityMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findByProviderAndProviderId(String provider, String providerId) {
        return userJpaRepository.findByProviderAndProviderId(Provider.fromString(provider), providerId)
                .map(userEntityMapper::toDomain);
    }

    @Override
    public boolean existsByProviderAndProviderId(String provider, String providerId) {
        return userJpaRepository.existsByProviderAndProviderId(Provider.fromString(provider), providerId);
    }

    @Override
    public Optional<User> findById(long id) {
        return userJpaRepository.findById(id)
                .map(userEntityMapper::toDomain);
    }
}
