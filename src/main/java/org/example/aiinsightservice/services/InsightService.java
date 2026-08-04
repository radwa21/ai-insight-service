package org.example.aiinsightservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aiinsightservice.dto.InsightResponseDto;
import org.example.aiinsightservice.model.InsightEntity;
import org.example.aiinsightservice.repository.InsightRepository;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class InsightService {

    private final LlmResilientClient llmResilientClient;
    private final InsightRepository insightRepository;

    public InsightResponseDto generateInsight(String content) {

        String hash = hashContent(content);

        Optional<InsightEntity> cached = insightRepository.findByContentHash(hash);
        if (cached.isPresent()) {
            log.info("Cache hit for content hash: {}", hash);
            return toDto(cached.get());
        }

        InsightResponseDto freshInsight = llmResilientClient.callLlmAndParse(content);
        saveToCache(hash, freshInsight);
        return freshInsight;
    }

    private void saveToCache(String hash, InsightResponseDto dto) {
        InsightEntity entity = InsightEntity.builder()
                .contentHash(hash)
                .summary(dto.getSummary())
                .tags(dto.getTags())
                .sentiment(dto.getSentiment())
                .createdAt(Instant.now())
                .build();
        insightRepository.save(entity);
    }

    private String hashContent(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(content.getBytes());
            return HexFormat.of().formatHex(hashBytes);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash content", e);
        }
    }

    private InsightResponseDto toDto(InsightEntity entity) {
        InsightResponseDto dto = new InsightResponseDto();
        dto.setSummary(entity.getSummary());
        dto.setTags(entity.getTags());
        dto.setSentiment(entity.getSentiment());
        return dto;
    }
}
