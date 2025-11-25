package com.ssafy.group.application.event;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class GroupEventHandlerFactoryImpl implements GroupEventHandlerFactory {

    private final Map<String, GroupEventHandler> handlers;

    public GroupEventHandlerFactoryImpl(List<GroupEventHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(GroupEventHandler::getEventType, Function.identity()));
    }

    @Override
    public GroupEventHandler getHandler(String eventType) {
        GroupEventHandler handler = handlers.get(eventType);
        if (handler == null) {
            throw new IllegalArgumentException("지원하지 않는 이벤트 타입: " + eventType);
        }
        return handler;
    }
}