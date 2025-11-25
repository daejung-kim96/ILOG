package ilog.back.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmotionAiClientImpl {
    private final WebClient emotionWebClient;

    public record AnalyzeReq(String text) {}
    public record AnalyzeRes(
            Map<String, Double> distribution,
            String summary,
            @JsonProperty("model_version") String modelVersion,
            @JsonProperty("llm_model") String llmModel,
            String source,
            double confidence
    ) {}

    public AnalyzeRes analyze(String text) {
        return emotionWebClient.post()
                .uri("/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new AnalyzeReq(text))
                .retrieve()
                .bodyToMono(AnalyzeRes.class)
                .block();
    }
}
