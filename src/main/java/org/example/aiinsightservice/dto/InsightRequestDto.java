package org.example.aiinsightservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InsightRequestDto {

    @NotBlank(message = "")
    @Size(max = 500, message = "Content must not exceed 5000 characters")
    String content;
}
