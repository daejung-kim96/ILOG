package com.ssafy.group.application.facade;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.ssafy.group.application.query.dto.GroupInfoModel;
import com.ssafy.group.application.query.service.GroupQueryService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GroupQueryFacade {

    private final GroupQueryService groupQueryService;

    public GroupInfoModel getInfo(long groupId) {
        return groupQueryService.getInfo(groupId);
    }

    public Set<GroupInfoModel> getGroup(long userId) {
        return groupQueryService.getGroup(userId);
    }

}
