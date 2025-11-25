package com.ssafy.user.application.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.user.application.event.dto.UserCreatedPayload;
import com.ssafy.user.application.facade.UserFacade;
import com.ssafy.user.common.exception.BusinessException;
import com.ssafy.user.common.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service // Application 계층의 Use Case
@RequiredArgsConstructor
public class UserCreatedHandler implements UserEventHandler {

    // 의존성: Write 리포지토리, Read 리포지토리, JSON 파서
    private final UserFacade userFacade;
    private final ObjectMapper objectMapper;

    @Override
    public String getEventType() {
        return "User_Created"; // 이 핸들러는 "User_Created"만 처리합니다.
    }

    /**
     * User_Created 이벤트의 실제 처리 로직
     * (이전 대화에서 합의한 대로) Write DB와 Read DB를 *모두* 업데이트합니다.
     */
    @Override
    @Transactional
    public void process(String payload) {
        UserCreatedPayload payloadDto = convertStringToPayload(payload);

        userFacade.createUser(payloadDto);

        log.info("User_Created 이벤트 처리 완료. Write/Read DB 저장 성공. User: {}", payloadDto.getName());
    }

    private UserCreatedPayload convertStringToPayload(String payload) {
        try {
            String unwrappedPayload = objectMapper.readValue(payload, String.class);
            return objectMapper.readValue(unwrappedPayload, UserCreatedPayload.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.USER_OUTBOX_PAYLOAD_UNSERIALIZE_FAILED);
        }
    }
}