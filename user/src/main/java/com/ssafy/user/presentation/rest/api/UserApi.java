package com.ssafy.user.presentation.rest.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ssafy.user.common.response.ApiResponse;
import com.ssafy.user.presentation.rest.dto.response.UserInfoResponse;

public interface UserApi {

    @GetMapping("/me")
    ApiResponse<UserInfoResponse> getMyInfo(@RequestHeader("X-User-Id") String userId);
}