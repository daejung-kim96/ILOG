package com.ssafy.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.ssafy.gateway.filter.FilterAuthProperties;
import com.ssafy.gateway.jwt.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties({ JwtProperties.class, FilterAuthProperties.class })
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

}
