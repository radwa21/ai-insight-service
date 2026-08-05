package org.example.aiinsightservice.services;

import org.example.aiinsightservice.dto.InsightResponseDto;
import org.example.aiinsightservice.exception.LlmServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
class LlmResilientClientTest {

    @MockitoBean
    private AnthropicApiClient anthropicApiClient;
    @Autowired
    private LlmResilientClient llmResilientClient;


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