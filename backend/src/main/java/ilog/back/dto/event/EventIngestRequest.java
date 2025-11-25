package ilog.back.dto.event;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class EventIngestRequest {
    @NotNull
    private Long cameraId;

    @NotNull
    private String type;

    private Map<String, Object> payload; // arbitrary JSON payload

    private LocalDateTime occurredAt; // optional, default now

    private Clip clip; // optional clip that may create a shorts

    @Data
    public static class Clip {
        private String url; // clip URL if already materialized
        private Long startMs; // optional
        private Long endMs;   // optional
    }
}

