package com.meterflow.dto;

import com.meterflow.entity.PlanType;

public class SubscriptionDtos {
    public record CreateSubscriptionRequest(PlanType planType) {}
    public record SubscriptionResponse(String merchantOrderId, String orderId, String redirectUrl, String state, String planType) {}
    public record VerifySubscriptionRequest(String merchantOrderId) {}
    public record VerifySubscriptionResponse(boolean success, String message, String state) {}
}
