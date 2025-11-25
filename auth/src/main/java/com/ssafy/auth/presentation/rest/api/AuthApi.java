package com.ssafy.auth.presentation.rest.api;

import org.springframework.web.bind.annotation.PostMapping;

import com.ssafy.auth.common.response.ApiResponse;

import org.springframework.http.ResponseEntity;

public interface AuthApi {

    @PostMapping("/reissue")
    ResponseEntity<ApiResponse<Void>> reissueRefreshToken(String refreshToken);

    @PostMapping("/logout")
    ResponseEntity<ApiResponse<Void>> logout(long userId);
}
