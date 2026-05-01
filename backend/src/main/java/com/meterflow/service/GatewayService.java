package com.meterflow.service;

import com.meterflow.entity.ApiKey;
import com.meterflow.entity.ApiKeyStatus;
import com.meterflow.entity.UsageLog;
import com.meterflow.repository.ApiKeyRepository;
import com.meterflow.repository.UsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class GatewayService {
    private final ApiKeyRepository apiKeyRepository;
    private final UsageLogRepository usageLogRepository;
    private final RateLimitService rateLimitService;
    private final RestTemplate restTemplate;

    public ResponseEntity<byte[]> forward(String apiName, String downstreamPath, String apiKeyValue,
                                          HttpMethod method, MultiValueMap<String, String> incomingHeaders, byte[] body) {
        ApiKey apiKey = apiKeyRepository.findByKeyValueAndStatus(apiKeyValue, ApiKeyStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Invalid API key"));
        if (!apiKey.getApi().getName().equalsIgnoreCase(apiName)) {
            throw new IllegalArgumentException("API key does not belong to this API");
        }
        if (!rateLimitService.allow(apiKey)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Rate limit exceeded".getBytes());
        }

        long started = System.currentTimeMillis();
        int status = 502;
        try {
            String targetUrl = apiKey.getApi().getBaseUrl() + downstreamPath;
            HttpHeaders headers = new HttpHeaders();
            headers.addAll(incomingHeaders);
            headers.remove("host");
            headers.remove("x-api-key");
            ResponseEntity<byte[]> response = restTemplate.exchange(targetUrl, method, new HttpEntity<>(body, headers), byte[].class);
            status = response.getStatusCode().value();
            return response;
        } catch (HttpStatusCodeException ex) {
            status = ex.getStatusCode().value();
            return ResponseEntity.status(ex.getStatusCode()).headers(ex.getResponseHeaders()).body(ex.getResponseBodyAsByteArray());
        } finally {
            usageLogRepository.save(UsageLog.builder()
                    .apiKey(apiKeyValue)
                    .endpoint("/gateway/" + apiName + downstreamPath)
                    .method(method.name())
                    .timestamp(Instant.now())
                    .statusCode(status)
                    .latencyMs(System.currentTimeMillis() - started)
                    .build());
        }
    }
}
