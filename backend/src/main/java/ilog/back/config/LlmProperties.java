package ilog.back.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm")
public class LlmProperties {
    private String base;
    private String key;
    private String model;
    private int timeoutMs = 10000;

    public String getBase() { return base; }
    public void setBase(String base) { this.base = base; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
}
