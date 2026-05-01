package com.meterflow.dto;

import com.meterflow.entity.ApiKeyStatus;
import com.meterflow.entity.PlanType;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public class ApiDtos {
    public record CreateApiRequest(@NotBlank String name, @NotBlank String baseUrl, String description) {}
    public record ApiResponse(String id, String name, String baseUrl, String description, Instant createdAt) {}
    public record GenerateKeyRequest(PlanType planType) {}
    public record ApiKeyResponse(String id, String apiId, String keyValue, ApiKeyStatus status, PlanType planType, Instant createdAt) {}
}
