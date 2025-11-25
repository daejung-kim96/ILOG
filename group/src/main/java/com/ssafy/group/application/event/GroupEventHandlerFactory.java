package com.ssafy.group.application.event;

public interface GroupEventHandlerFactory {
    GroupEventHandler getHandler(String eventType);
}