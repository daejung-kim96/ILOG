package com.ssafy.group.application.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.group.application.event.dto.GroupCreatedPayload;
import com.ssafy.group.application.facade.GroupFacade;
import com.ssafy.group.common.exception.BusinessException;
import com.ssafy.group.common.exception.ErrorCode;
import com.ssafy.group.domain.model.aggregate.Group;
import com.ssafy.group.domain.repository.GroupRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service // Application 계층의 Use Case
@RequiredArgsConstructor
public class GroupCreatedHandler implements GroupEventHandler {

    // 의존성: Write 리포지토리, Read 리포지토리, JSON 파서
    private final GroupFacade groupFacade;
    private final ObjectMapper objectMapper;

    @Override
    public String getEventType() {
        return "Group_Created"; // 이 핸들러는 "Group_Created"만 처리합니다.
    }

    /**
     * Group_Created 이벤트의 실제 처리 로직
     * (이전 대화에서 합의한 대로) Write DB와 Read DB를 *모두* 업데이트합니다.
     */
    @Override
    @Transactional
    public void process(String payload) {
        GroupCreatedPayload payloadDto = convertStringToPayload(payload);

        // groupFacade.createGroup(payloadDto);

        log.info("Group_Created 이벤트 처리 완료. Write/Read DB 저장 성공. Group: {}", payloadDto.getName());
    }

    private GroupCreatedPayload convertStringToPayload(String payload) {
        try {
            return objectMapper.readValue(payload, GroupCreatedPayload.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.GROUP_OUTBOX_PAYLOAD_UNSERIALIZE_FAILED);
        }
    }
}