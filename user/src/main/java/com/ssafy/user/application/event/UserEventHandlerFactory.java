package com.ssafy.user.application.event;

public interface UserEventHandlerFactory {
    UserEventHandler getHandler(String eventType);
}