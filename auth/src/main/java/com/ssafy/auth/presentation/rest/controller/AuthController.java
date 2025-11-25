package com.ssafy.auth.presentation.rest.controller;

import com.ssafy.auth.presentation.mapper.AuthMapper;
import com.ssafy.auth.presentation.rest.api.AuthApi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.auth.application.facade.AuthFacade;
import com.ssafy.auth.common.response.ApiResponse;
import com.ssafy.auth.presentation.rest.dto.response.TokenResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthApi {

        private final AuthFacade authFacade;
        private final AuthMapper authMapper;

        @PostMapping("/reissue")
        public ResponseEntity<ApiResponse<Void>> reissueRefreshToken(
                        @CookieValue("refresh_token") String refreshToken) {

                TokenResponse tokenResponse = authMapper.toTokenResponse(authFacade.reissueRefreshToken(
                                refreshToken));

                ResponseCookie accessCookie = ResponseCookie.from("access_token", tokenResponse.accessToken())
                                .path("/")
                                .httpOnly(true)
                                .secure(true)
                                .maxAge(60 * 15)
                                .sameSite("Lax")
                                .build();

                ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokenResponse.refreshToken())
                                .path("/")
                                .httpOnly(true)
                                .secure(true)
                                .maxAge(60 * 60 * 24 * 7)
                                .sameSite("Lax")
                                .build();

                return ResponseEntity.ok()
                                .header("Set-Cookie", accessCookie.toString())
                                .header("Set-Cookie", refreshCookie.toString())
                                .body(ApiResponse.of(null));
        }

        @PostMapping("/logout/{userId}")
        public ResponseEntity<ApiResponse<Void>> logout(@PathVariable("userId") long userId) {

                authFacade.logout(userId);

                ResponseCookie accessCookie = ResponseCookie.from("access_token", "")
                                .path("/")
                                .httpOnly(true)
                                .secure(true)
                                .maxAge(0)
                                .sameSite("Lax")
                                .build();

                ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
                                .path("/")
                                .httpOnly(true)
                                .secure(true)
                                .maxAge(0)
                                .sameSite("Lax")
                                .build();

                return ResponseEntity.ok()
                                .header("Set-Cookie", accessCookie.toString())
                                .header("Set-Cookie", refreshCookie.toString())
                                .body(ApiResponse.of(null));
        }

}
