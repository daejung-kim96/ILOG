package com.ssafy.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.ssafy.auth.application.oauth2.OAuth2Properties;
import com.ssafy.auth.infrastructure.jwt.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties({ JwtProperties.class, OAuth2Properties.class })
public class AuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
	}

}
