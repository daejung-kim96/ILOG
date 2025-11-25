package ilog.back.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WorkerClient {

    @Qualifier("workerWebClient")
    private final WebClient workerWebClient;

    public void putConfig(Map<String, Object> payload) {
        workerWebClient
                .put()
                .uri("/config")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .onErrorResume(ex -> Mono.error(new RuntimeException("Worker /config call failed", ex)))
                .block();
    }

    public Map<String, Object> fetchSignals() {
        return workerWebClient
            .get()
            .uri("/signals")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .onErrorResume(ex -> Mono.error(new RuntimeException("Worker /signals call failed", ex)))
            .block();
    }

    public byte[] fetchPreview() {
        return workerWebClient
            .get()
            .uri("/preview.jpg")
            .accept(MediaType.IMAGE_JPEG)
            .retrieve()
            .bodyToMono(byte[].class)
            .onErrorResume(ex -> Mono.error(new RuntimeException("Worker /preview.jpg call failed", ex)))
            .block();
    }
}
