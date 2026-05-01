package com.meterflow.dto;

import com.meterflow.entity.PlanType;

public class SubscriptionDtos {
    public record CreateSubscriptionRequest(PlanType planType) {}
    public record SubscriptionResponse(String subscriptionId, String planType) {}
    public record VerifySubscriptionRequest(String paymentId, String subscriptionId, String signature) {}
    public record VerifySubscriptionResponse(boolean success, String message) {}
}
