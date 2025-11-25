package com.ssafy.user.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.ssafy.user.application.event.UserEventHandlerFactory;
import com.ssafy.user.common.exception.BusinessException;
import com.ssafy.user.common.exception.ErrorCode;

@Slf4j
@Component // 인프라 계층의 Adapter
@RequiredArgsConstructor
public class UserEventConsumer {

    // Application 계층의 팩토리(Port)를 주입받습니다.
    private final UserEventHandlerFactory eventHandlerFactory;

    /**
     * "User" 토픽을 구독합니다.
     * Auth 서버가 발행한 "User_Created", "User_Updated" 등 모든 User 관련 이벤트가
     * 이 리스너로 들어옵니다.
     */
    @KafkaListener(topics = "outbox.event.User")
    public void handleUserEvents(@Payload String payload,
            @Header("X-EventType") String eventType) {

        log.info("[KAFKA-SUB] User 이벤트 수신. Type: {}", eventType);

        try {
            // 1. 팩토리(Application)에서 eventType에 맞는 핸들러를 가져옵니다.
            // (예: eventType이 "User_Created"이면 UserCreatedHandler를 반환)
            var handler = eventHandlerFactory.getHandler(eventType);

            // 2. 핸들러(Application)에게 로직 처리 위임
            handler.process(payload);

        } catch (Exception e) {
            log.error("[KAFKA-SUB] 이벤트 처리 중 오류 발생. Type: {}, Payload: {}", eventType, payload, e);
            throw new BusinessException(ErrorCode.USER_KAFKA_EVENT_ERROR);
        }
    }
}