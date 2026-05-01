package com.meterflow.service;

import com.meterflow.entity.ApiKey;
import com.meterflow.entity.PlanType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RateLimitService {
    private final StringRedisTemplate redisTemplate;
    private final boolean redisEnabled;
    private final Map<String, AtomicLong> localCounters = new ConcurrentHashMap<>();

    public RateLimitService(StringRedisTemplate redisTemplate,
                            @Value("${app.rate-limit.redis-enabled:true}") boolean redisEnabled) {
        this.redisTemplate = redisTemplate;
        this.redisEnabled = redisEnabled;
    }

    public boolean allow(ApiKey apiKey) {
        long limit = apiKey.getPlanType() == PlanType.PRO ? 1000 : 100;
        String key = "rate:" + apiKey.getKeyValue() + ":" + LocalDate.now();
        if (!redisEnabled) {
            return localCounters.computeIfAbsent(key, ignored -> new AtomicLong()).incrementAndGet() <= limit;
        }

        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofDays(1));
        }
        return count == null || count <= limit;
    }
}
