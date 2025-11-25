package com.ssafy.user.application.query.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.user.application.query.dto.UserInfoModel;
import com.ssafy.user.common.exception.BusinessException;
import com.ssafy.user.common.exception.ErrorCode;
import com.ssafy.user.infrastructure.read.persistence.jpa.entity.UserReadEntity;
import com.ssafy.user.infrastructure.read.persistence.jpa.mapper.UserReadEntityMapper;
import com.ssafy.user.infrastructure.read.persistence.jpa.repository.UserReadRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryService {

    private final UserReadRepository userReadRepository;
    private final UserReadEntityMapper userReadEntityMapper;

    public UserInfoModel getInfo(long userId) {
        UserReadEntity userReadEntity = userReadRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_READ_USER_NOT_FOUND));

        return userReadEntityMapper.toModel(userReadEntity);
    }
}
