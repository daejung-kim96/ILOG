package com.ssafy.gateway.filter;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway.filter.auth")
public record FilterAuthProperties(List<String> permitAllPaths) {
}
