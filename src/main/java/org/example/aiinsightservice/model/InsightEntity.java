package org.example.aiinsightservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "insights")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String contentHash;

    private String summary;
    private List<String> tags;
    private String sentiment;

    private Instant createdAt;
}