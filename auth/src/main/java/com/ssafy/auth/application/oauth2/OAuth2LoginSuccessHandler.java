package com.ssafy.auth.application.oauth2;

import java.io.IOException;

import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.ssafy.auth.domain.model.aggregate.User;
import com.ssafy.auth.infrastructure.jwt.JwtUtil;
import com.ssafy.auth.infrastructure.service.RedisService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

        private final JwtUtil jwtUtil;
        private final OAuth2Properties oAuth2Properties;
        private final RedisService redisService;

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                        Authentication authentication) throws IOException, ServletException {
                PrincipalDetails oAuth2User = (PrincipalDetails) authentication.getPrincipal();
                User user = oAuth2User.getUser();

                String accessToken = jwtUtil.createAccessToken(user.getId().value());
                String refreshToken = jwtUtil.createRefreshToken(user.getId().value());
                redisService.saveRefreshToken(user.getId().value(), refreshToken);

                ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
                                .path("/")
                                .httpOnly(true)
                                .secure(true)
                                .maxAge(60 * 15)
                                .sameSite("Lax")
                                .build();

                ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                                .path("/")
                                .httpOnly(true)
                                .secure(true)
                                .maxAge(60 * 60 * 24 * 7)
                                .sameSite("Lax")
                                .build();

                response.addHeader("Set-Cookie", accessCookie.toString());
                response.addHeader("Set-Cookie", refreshCookie.toString());

                String targetUrl = UriComponentsBuilder.fromUriString(oAuth2Properties.url() + "/")
                                .build().toUriString();

                getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
}
