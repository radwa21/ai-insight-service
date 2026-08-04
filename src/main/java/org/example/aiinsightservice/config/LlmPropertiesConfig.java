package org.example.aiinsightservice.config;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "llm.api")
@Getter
@Setter
public class LlmPropertiesConfig {
    private String baseUrl;
    private String apiKey;
    private String model;
    private int maxTokens;
    private int timeoutSeconds;
    private boolean mockEnabled;
}
