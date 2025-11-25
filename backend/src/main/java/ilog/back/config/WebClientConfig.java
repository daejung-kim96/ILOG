package ilog.back.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;

// webFlux를 사용하는 WebClient, 외부 서버에 API 요청을 보내는데 사용

// 감정분석용
@Configuration
public class WebClientConfig {
        @Bean
        WebClient emotionWebClient(
                        @Value("${ai.emotion.base-url}") String baseUrl,
                        @Value("${ai.emotion.timeout-ms:5000}") long timeoutMs) {
                HttpClient http = HttpClient.create().responseTimeout(Duration.ofMillis(timeoutMs));
                return WebClient.builder()
                                .baseUrl(baseUrl)
                                .clientConnector(new ReactorClientHttpConnector(http))
                                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .build();
        }

        // LLM(OpenAI) 용
        @Bean
        WebClient llmWebClient(ilog.back.config.LlmProperties props) {
                HttpClient http = HttpClient.create()
                                .responseTimeout(Duration.ofMillis(props.getTimeoutMs()));
                return WebClient.builder()
                                .baseUrl(props.getBase())
                                .clientConnector(new ReactorClientHttpConnector(http))
                                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.getKey())
                                .build();
        }

        @Bean
        WebClient workerWebClient(ilog.back.config.WorkerProperties props) {
                long timeoutMs = props.getTimeoutMs();
                HttpClient http = HttpClient.create().responseTimeout(Duration.ofMillis(timeoutMs));
                return WebClient.builder()
                                .baseUrl(props.getBaseUrl())
                                .clientConnector(new ReactorClientHttpConnector(http))
                                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .build();
        }
}
