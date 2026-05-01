package com.meterflow.repository;

import com.meterflow.entity.UsageLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface UsageLogRepository extends MongoRepository<UsageLog, String> {
    long countByApiKeyAndTimestampBetween(String apiKey, Instant start, Instant end);

    @Query("""
            select count(l) from UsageLog l
            where l.apiKey in (
                select k.keyValue from ApiKey k where k.api.user.id = :userId
            ) and l.timestamp between :start and :end
            """)
    long countForUserBetween(@Param("userId") Long userId, @Param("start") Instant start, @Param("end") Instant end);
}
