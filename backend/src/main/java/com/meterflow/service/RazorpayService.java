package com.meterflow.service;

import com.meterflow.entity.PlanType;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RazorpayService {

    @Value("${app.razorpay.key_id}")
    private String keyId;

    @Value("${app.razorpay.key_secret}")
    private String keySecret;

    @Value("${app.razorpay.pro_plan_id}")
    private String proPlanId;

    public String createSubscription(PlanType planType, int totalCount) throws RazorpayException {
        String planId = resolvePlanId(planType);
        RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);
        JSONObject subscriptionRequest = new JSONObject();
        subscriptionRequest.put("plan_id", planId);
        subscriptionRequest.put("total_count", totalCount);
        subscriptionRequest.put("customer_notify", 1);
        
        com.razorpay.Subscription subscription = razorpayClient.subscriptions.create(subscriptionRequest);
        return subscription.get("id");
    }

    public boolean verifySignature(String paymentId, String subscriptionId, String signature) {
        try {
            String payload = paymentId + "|" + subscriptionId;
            String generatedSignature = com.razorpay.Utils.calculateRFC2104HMAC(payload, keySecret);
            return generatedSignature.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private String resolvePlanId(PlanType planType) {
        if (planType == PlanType.PRO && proPlanId != null && !proPlanId.isBlank()) {
            return proPlanId;
        }
        throw new IllegalArgumentException("Razorpay plan is not configured");
    }
}
