package org.example.aiinsightservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.aiinsightservice.dto.InsightRequestDto;
import org.example.aiinsightservice.dto.InsightResponseDto;
import org.example.aiinsightservice.services.InsightService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/insights")
@RequiredArgsConstructor
public class ContentInsightController {
    private final InsightService insightService;

    @PostMapping
    public ResponseEntity<InsightResponseDto> generateInsight(
            @Valid @RequestBody InsightRequestDto request) {

        InsightResponseDto response = insightService.generateInsight(request.getContent());
        return ResponseEntity.ok(response);
    }
}