package com.ssafy.auth.domain.repository;

import com.ssafy.auth.domain.model.aggregate.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    boolean existsByProviderAndProviderId(String provider, String providerId);

    Optional<User> findById(long id);
}