package org.example.aiinsightservice.repository;

import org.example.aiinsightservice.model.InsightEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface InsightRepository extends MongoRepository<InsightEntity, String> {

    Optional<InsightEntity> findByContentHash(String contentHash);
}