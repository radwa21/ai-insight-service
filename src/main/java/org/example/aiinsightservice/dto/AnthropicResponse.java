package org.example.aiinsightservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AnthropicResponse {

    private String id;
    private String model;
    private List<ContentBlock> content;

    @Data
    @NoArgsConstructor
    public static class ContentBlock {
        private String type;
        private String text;
    }
}