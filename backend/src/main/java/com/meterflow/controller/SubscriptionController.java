package com.meterflow.controller;

import com.meterflow.dto.SubscriptionDtos.*;
import com.meterflow.entity.PlanType;
import com.meterflow.repository.UserRepository;
import com.meterflow.security.PrincipalUser;
import com.meterflow.service.PhonePeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final PhonePeService phonePeService;
    private final UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createSubscription(@RequestBody CreateSubscriptionRequest request,
                                                @AuthenticationPrincipal PrincipalUser principal) {
        try {
            PlanType planType = request.planType() == null ? PlanType.PRO : request.planType();
            SubscriptionResponse response = phonePeService.createProPayment(principal.user(), planType);
            var user = principal.user();
            user.setPhonepeMerchantOrderId(response.merchantOrderId());
            user.setSubscriptionStatus("PAYMENT_PENDING");
            userRepository.save(user);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifySubscriptionResponse> verifySubscription(@RequestBody VerifySubscriptionRequest request,
                                                                         @AuthenticationPrincipal PrincipalUser principal) {
        try {
            String state = phonePeService.orderState(request.merchantOrderId());
            if ("COMPLETED".equalsIgnoreCase(state)) {
                var user = principal.user();
                user.setPlanType(PlanType.PRO);
                user.setPhonepeMerchantOrderId(request.merchantOrderId());
                user.setSubscriptionStatus("ACTIVE");
                user.setSubscriptionVerifiedAt(Instant.now());
                userRepository.save(user);
                return ResponseEntity.ok(new VerifySubscriptionResponse(true, "PhonePe payment verified successfully", state));
            }
            return ResponseEntity.badRequest().body(new VerifySubscriptionResponse(false, "PhonePe payment is " + state, state));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new VerifySubscriptionResponse(false, e.getMessage(), "UNKNOWN"));
        }
    }
}
