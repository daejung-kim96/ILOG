package com.ssafy.group.presentation.rest.api;

import java.util.Set;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.ssafy.group.common.response.ApiResponse;
import com.ssafy.group.presentation.rest.dto.request.CreateGroupRequestBody;
import com.ssafy.group.presentation.rest.dto.request.InviteGroupRequestBody;
import com.ssafy.group.presentation.rest.dto.response.GroupResponse;

public interface GroupApi {
        @PostMapping("/{name}")
        ApiResponse<GroupResponse> createGroup(@RequestHeader("X-User-Id") String userId,
                        @RequestBody CreateGroupRequestBody createGroupRequestBody);

        @PostMapping("/invite/{inviteCode}")
        ApiResponse<GroupResponse> inviteGroup(@RequestHeader("X-User-Id") String userId,
                        @RequestBody InviteGroupRequestBody inviteGroupRequestBody);

        @GetMapping()
        ApiResponse<Set<GroupResponse>> getGroup(@RequestHeader("X-User-Id") String userId);
}