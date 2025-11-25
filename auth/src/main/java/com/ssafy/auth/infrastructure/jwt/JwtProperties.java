package com.ssafy.auth.infrastructure.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
                String eccPrivateKey,
                String eccPublicKey,
                long accessTokenValidityInSeconds,
                long refreshTokenValidityInSeconds) {
}