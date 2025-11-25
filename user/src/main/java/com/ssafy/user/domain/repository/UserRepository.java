package com.ssafy.user.domain.repository;

import java.util.Optional;

import com.ssafy.user.domain.model.aggregate.User;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(long userId);
}