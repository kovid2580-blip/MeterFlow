package com.meterflow.repository;

import com.meterflow.entity.UsageLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.Collection;

public interface UsageLogRepository extends MongoRepository<UsageLog, String> {
    long countByApiKeyAndTimestampBetween(String apiKey, Instant start, Instant end);
    long countByApiKeyInAndTimestampBetween(Collection<String> apiKeys, Instant start, Instant end);
}
