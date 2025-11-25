package com.ssafy.user.infrastructure.write.persistence.jpa.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ssafy.user.domain.model.aggregate.User;
import com.ssafy.user.domain.repository.UserRepository;
import com.ssafy.user.infrastructure.write.persistence.jpa.mapper.UserEntityMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository userJpaRepository;
    private final UserEntityMapper userEntityMapper;

    @Override
    public User save(User user) {
        return userEntityMapper.toDomain(userJpaRepository.save(userEntityMapper.toEntity(user)));
    }

    @Override
    public Optional<User> findById(long userId) {
        return userJpaRepository.findById(userId).map(userEntityMapper::toDomain);
    }
}
