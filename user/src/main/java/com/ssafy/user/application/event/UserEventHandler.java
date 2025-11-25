package com.ssafy.user.application.event;

public interface UserEventHandler {
    String getEventType(); // 자신이 처리할 이벤트 타입

    void process(String payload); // 이벤트 처리 로직
}