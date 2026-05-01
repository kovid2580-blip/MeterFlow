package com.meterflow.controller;

import com.meterflow.dto.SubscriptionDtos.*;
import com.meterflow.entity.PlanType;
import com.meterflow.repository.UserRepository;
import com.meterflow.security.PrincipalUser;
import com.meterflow.service.RazorpayService;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final RazorpayService razorpayService;
    private final UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<SubscriptionResponse> createSubscription(@RequestBody CreateSubscriptionRequest request,
                                                                   @AuthenticationPrincipal PrincipalUser principal) {
        try {
            PlanType planType = request.planType() == null ? PlanType.PRO : request.planType();
            String subscriptionId = razorpayService.createSubscription(planType, 12);
            return ResponseEntity.ok(new SubscriptionResponse(subscriptionId, planType.name()));
        } catch (RazorpayException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifySubscriptionResponse> verifySubscription(@RequestBody VerifySubscriptionRequest request,
                                                                         @AuthenticationPrincipal PrincipalUser principal) {
        boolean isValid = razorpayService.verifySignature(request.paymentId(), request.subscriptionId(), request.signature());
        if (isValid) {
            var user = principal.user();
            user.setPlanType(PlanType.PRO);
            user.setRazorpaySubscriptionId(request.subscriptionId());
            user.setSubscriptionStatus("ACTIVE");
            user.setSubscriptionVerifiedAt(Instant.now());
            userRepository.save(user);
            return ResponseEntity.ok(new VerifySubscriptionResponse(true, "Subscription verified successfully"));
        } else {
            return ResponseEntity.badRequest().body(new VerifySubscriptionResponse(false, "Invalid signature"));
        }
    }
}
