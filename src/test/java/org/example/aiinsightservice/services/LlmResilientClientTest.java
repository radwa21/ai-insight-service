package org.example.aiinsightservice.services;

import org.example.aiinsightservice.dto.InsightResponseDto;
import org.example.aiinsightservice.exception.LlmServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class LlmResilientClientTest {

    @Mock
    private AnthropicApiClient anthropicApiClient;

    private LlmResilientClient llmResilientClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        llmResilientClient = new LlmResilientClient(anthropicApiClient, new ObjectMapper());
    }

    @Test
    void whenAnthropicApiFails_thenFallbackIsReturned() {
        when(anthropicApiClient.sendMessage(anyString()))
                .thenThrow(new LlmServiceException("Simulated API failure"));

        InsightResponseDto result = llmResilientClient.callLlmAndParse("some test content");

        assertThat(result.getSummary()).isEqualTo("Insight temporarily unavailable");
        assertThat(result.getSentiment()).isEqualTo("neutral");
        assertThat(result.getTags()).isEmpty();
    }
}