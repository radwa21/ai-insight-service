package org.example.aiinsightservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class InsightResponseDto {
    private String summary;
    private List<String> tags;
    private String sentiment;
}
