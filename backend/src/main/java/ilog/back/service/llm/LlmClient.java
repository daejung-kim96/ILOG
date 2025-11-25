package ilog.back.service.llm;

import ilog.back.config.LlmProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LlmClient {

    private final WebClient llmWebClient;
    private final LlmProperties props;

    // 보고서 요약 전용
    public String summarizeReport(
            String periodKo, String start, String end,
            Map<String, Double> avg,
            List<Map<String, Object>> daily,
            Map<String, Object> trend
    ) {
        String system = """
            너는 한국어 감정 리포트 요약가다.
            - 반드시 한국어로 2~3문장 요약만 반환한다.
            - 조언/진단/명령 금지. 관찰된 경향만 간결히.
            - JSON/마크다운/코드블록 없이 순수 텍스트만.
        """;

        String user = """
            기간: %s (%s ~ %s)
            평균(avg, 0~1): %s
            일별(daily 일부): %s
            추세(trend): %s
        """.formatted(periodKo, start, end, avg, preview(daily), trend);

        Map<String,Object> body = Map.of(
                "model", props.getModel(),
                "messages", List.of(
                        Map.of("role","system","content", system),
                        Map.of("role","user","content", user)
                )
        );

        try {
            Map<?,?> res = llmWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (res == null) return null;
            var choices = (List<?>) res.get("choices");
            if (choices == null || choices.isEmpty()) return null;
            var first = (Map<?,?>) choices.get(0);
            var message = (Map<?,?>) first.get("message");
            var content = message == null ? null : message.get("content");
            return content == null ? null : content.toString().trim();
        } catch (Exception e) {
            return null;
        }
    }

    private String preview(List<Map<String, Object>> daily) {
        if (daily == null || daily.isEmpty()) return "[]";
        int n = Math.min(daily.size(), 7);
        var slim = daily.subList(0, n).stream().map(d -> Map.of(
                "day", d.get("day"),
                "happy", d.get("happy"),
                "sad", d.get("sad"),
                "anxious", d.get("anxious")
        )).toList();
        return slim.toString();
    }
}

