package ilog.back.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "worker")
public class WorkerProperties {
    /**
     * Base URL of the Video-AI Worker (FastAPI), e.g. http://ai-worker:9108
     */
    private String baseUrl;

    /**
     * HTTP timeout in milliseconds.
     */
    private long timeoutMs = 5000L;
}

