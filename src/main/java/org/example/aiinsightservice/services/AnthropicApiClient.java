package org.example.aiinsightservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aiinsightservice.config.LlmPropertiesConfig;
import org.example.aiinsightservice.config.WebClientConfig;
import org.example.aiinsightservice.dto.AnthropicRequest;
import org.example.aiinsightservice.dto.AnthropicResponse;
import org.example.aiinsightservice.exception.LlmServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AnthropicApiClient {

    private final WebClient anthropicWebClient;
    private final LlmPropertiesConfig llmProperties;

    public AnthropicResponse sendMessage(String userContent) {

        if (llmProperties.isMockEnabled()) {
            return buildMockResponse();
        }

        AnthropicRequest request = AnthropicRequest.builder()
                .model(llmProperties.getModel())
                .maxTokens(llmProperties.getMaxTokens())
                .messages(List.of(
                        AnthropicRequest.AnthropicMessage.builder()
                                .role("user")
                                .content(userContent)
                                .build()
                ))
                .build();

        try {
            return anthropicWebClient.post()
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AnthropicResponse.class)
                    .timeout(Duration.ofSeconds(llmProperties.getTimeoutSeconds()))
                    .block();

        } catch (WebClientResponseException e) {
            log.error("Anthropic API returned error status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new LlmServiceException("LLM API call failed with status: " + e.getStatusCode(), e);

        } catch (Exception e) {
            log.error("Unexpected error calling Anthropic API", e);
            throw new LlmServiceException("Unexpected error during LLM call", e);
        }
    }

    private AnthropicResponse buildMockResponse() {
        log.warn("MOCK MODE ENABLED — returning canned response instead of calling Anthropic API");

        AnthropicResponse mock = new AnthropicResponse();
        AnthropicResponse.ContentBlock block = new AnthropicResponse.ContentBlock();
        block.setType("text");
        block.setText("""
                {"summary": "User expresses satisfaction with a skincare routine.", "tags": ["skincare", "positive-experience"], "sentiment": "positive"}
                """);
        mock.setContent(List.of(block));
        return mock;
    }
}
