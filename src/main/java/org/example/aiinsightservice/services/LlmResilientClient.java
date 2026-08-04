package org.example.aiinsightservice.services;

import tools.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aiinsightservice.dto.AnthropicResponse;
import org.example.aiinsightservice.dto.InsightResponseDto;
import org.example.aiinsightservice.exception.LlmServiceException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class LlmResilientClient {

    private final AnthropicApiClient anthropicApiClient;
    private final ObjectMapper objectMapper;

    private static final String PROMPT_TEMPLATE = """
            Analyze the following text and respond ONLY with valid JSON, no other text, no markdown code blocks.
            The JSON must match this exact schema:
            {"summary": "one sentence summary", "tags": ["tag1", "tag2"], "sentiment": "positive|negative|neutral"}

            Text to analyze:
            %s
            """;

    @CircuitBreaker(name = "llmService", fallbackMethod = "fallbackInsight")
    @Retry(name = "llmService")
    @RateLimiter(name = "llmService")
    public InsightResponseDto callLlmAndParse(String content) {
        String prompt = PROMPT_TEMPLATE.formatted(content);
        AnthropicResponse rawResponse = anthropicApiClient.sendMessage(prompt);
        String jsonText = extractTextFromResponse(rawResponse);
        return parseInsightResponse(jsonText);
    }

    private String extractTextFromResponse(AnthropicResponse response) {
        if (response.getContent() == null || response.getContent().isEmpty()) {
            throw new LlmServiceException("Empty response from LLM");
        }
        return response.getContent().get(0).getText();
    }

    private InsightResponseDto parseInsightResponse(String jsonText) {
        try {
            return objectMapper.readValue(jsonText, InsightResponseDto.class);
        } catch (Exception e) {
            log.error("Failed to parse LLM JSON response: {}", jsonText, e);
            throw new LlmServiceException("Malformed JSON returned by LLM", e);
        }
    }

    private InsightResponseDto fallbackInsight(String content, Throwable throwable) {
        log.warn("LLM service unavailable, returning fallback insight. Reason: {}", throwable.getMessage());
        InsightResponseDto fallback = new InsightResponseDto();
        fallback.setSummary("Insight temporarily unavailable");
        fallback.setTags(Collections.emptyList());
        fallback.setSentiment("neutral");
        return fallback;
    }
}