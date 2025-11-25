package com.ssafy.auth.infrastructure.write.persistence.jpa.repository.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssafy.auth.infrastructure.write.persistence.jpa.entity.outbox.Outbox;

import java.util.UUID;

public interface OutboxRepository extends JpaRepository<Outbox, UUID> {
}
