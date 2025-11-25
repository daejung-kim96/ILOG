package com.ssafy.auth.application.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.auth.common.exception.BusinessException;
import com.ssafy.auth.common.exception.ErrorCode;
import com.ssafy.auth.domain.model.aggregate.User;
import com.ssafy.auth.domain.repository.UserRepository;
import com.ssafy.auth.infrastructure.write.persistence.jpa.entity.outbox.Outbox;
import com.ssafy.auth.infrastructure.write.persistence.jpa.repository.outbox.OutboxRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserCommand {
    private final UserRepository userRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public User createUser(String email, String provider, String providerId, String name, String picture) {

        User user = User.oauth2SignUp(provider, providerId);
        User savedUser = userRepository.save(user);

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", savedUser.getId().value());
        payload.put("email", email);
        payload.put("name", name);
        payload.put("picture", picture);
        payload.put("provider", savedUser.getProvider().name());
        payload.put("providerId", savedUser.getProviderId());
        Outbox outbox = Outbox.builder()
                .aggregateType("User")
                .aggregateId(String.valueOf(savedUser.getId().value()))
                .eventType("User_Created")
                .payload(convertPayloadToString(payload))
                .createdAt(LocalDateTime.now())
                .build();

        outboxRepository.save(outbox);

        return savedUser;
    }

    private String convertPayloadToString(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.AUTH_OUTBOX_PAYLOAD_SERIALIZE_FAILED, e);
        }
    }
}
