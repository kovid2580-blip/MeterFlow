package com.meterflow.dto;

import com.meterflow.entity.PaymentStatus;

import java.math.BigDecimal;

public class BillingDtos {
    public record BillingResponse(String id, String month, long totalRequests, BigDecimal amount, PaymentStatus paymentStatus) {}
}
