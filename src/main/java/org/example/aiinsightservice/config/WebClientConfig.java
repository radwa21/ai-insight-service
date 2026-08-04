package org.example.aiinsightservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
/// create object from webclient
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {
    private final LlmPropertiesConfig llmPropertiesConfig;
    @Bean
    public WebClient anthropicWebClient() {
        return WebClient.builder()
                .baseUrl(llmPropertiesConfig.getBaseUrl())
                .defaultHeader("x-api-key", llmPropertiesConfig.getApiKey())
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();


    }
}
