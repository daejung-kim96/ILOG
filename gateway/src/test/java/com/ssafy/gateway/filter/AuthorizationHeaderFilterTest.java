package com.ssafy.gateway.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@org.springframework.test.context.TestPropertySource(properties = {
    "jwt.ecc-public-key=MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAELcKVjGS6GlZkEdhPwUHBFlkGbnoeEaFt3uJO6K471sP6crF1iYtwWgsB2wqoVWTJt/v/sYUt/Gl1dgHi912W3g=="
})
public class AuthorizationHeaderFilterTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("보호되지 않은 요청은 인증 필터를 통과한다")
    void nonProtectedRequest_Passes_Through_Filter() {
        String nonProtectedUrl = "/api/v1/auth/login";

        webTestClient.post().uri(nonProtectedUrl)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("액세스 토큰 없이 보호된 API 요청 시 401 에러를 반환한다")
    void unauthorizedWhenNoToken() {
        String protectedUrl = "/api/v1/users/me";

        webTestClient.get().uri(protectedUrl)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("유효하지 않은 액세스 토큰으로 보호된 API 요청 시 401 에러를 반환한다")
    void unauthorizedWhenInvalidToken() {
        String protectedUrl = "/api/v1/users/me";
        String invalidToken = "invalid.token.string";

        webTestClient.get().uri(protectedUrl)
                .cookie("access_token", invalidToken)
                .exchange()
                .expectStatus().isUnauthorized();

    }

    // @Test
    // @DisplayName("유효액세스 토큰으로 보호된 API 요청을 통과한다")
    // void authorizedWhenValidToken() {
    // String protectedUrl = "/api/v1/users/me";
    // String validToken = "a-real-valid-token-signed-with-private-key";

    // webTestClient.get().uri(protectedUrl)
    // .cookie("access_token", validToken)
    // .exchange()
    // .expectStatus().isNotFound();
    // }
}
