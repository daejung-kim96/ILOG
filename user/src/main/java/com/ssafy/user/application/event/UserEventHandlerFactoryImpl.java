package com.ssafy.user.application.event;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UserEventHandlerFactoryImpl implements UserEventHandlerFactory {

    private final Map<String, UserEventHandler> handlers;

    public UserEventHandlerFactoryImpl(List<UserEventHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(UserEventHandler::getEventType, Function.identity()));
    }

    @Override
    public UserEventHandler getHandler(String eventType) {
        UserEventHandler handler = handlers.get(eventType);
        if (handler == null) {
            throw new IllegalArgumentException("지원하지 않는 이벤트 타입: " + eventType);
        }
        return handler;
    }
}