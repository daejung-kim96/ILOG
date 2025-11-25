package com.ssafy.auth.application.oauth2;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "domain")
public record OAuth2Properties(
        String url) {
}
