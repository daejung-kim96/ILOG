package com.ssafy.group.application.query.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.group.application.query.dto.GroupInfoModel;
import com.ssafy.group.common.exception.BusinessException;
import com.ssafy.group.common.exception.ErrorCode;
import com.ssafy.group.infrastructure.read.persistence.jpa.entity.GroupReadEntity;
import com.ssafy.group.infrastructure.read.persistence.jpa.mapper.GroupInfoModelMapper;
import com.ssafy.group.infrastructure.read.persistence.jpa.repository.GroupReadRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GroupQueryService {

    private final GroupReadRepository groupReadRepository;
    private final GroupInfoModelMapper groupInfoModelMapper;

    public GroupInfoModel getInfo(long groupId) {
        GroupReadEntity groupReadEntity = groupReadRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_READ_GROUP_NOT_FOUND));
        return groupInfoModelMapper.toGroupInfoModel(groupReadEntity);
    }

    public Set<GroupInfoModel> getGroup(long userId) {
        Set<GroupReadEntity> groupReadEntities = groupReadRepository.findByGroupMembers_UserId(userId);
        return groupReadEntities.stream()
                .map(groupInfoModelMapper::toGroupInfoModel)
                .collect(Collectors.toSet());
    }

}
