package com.meterflow.controller;

import com.meterflow.dto.BillingDtos.BillingResponse;
import com.meterflow.security.PrincipalUser;
import com.meterflow.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
public class BillingController {
    private final BillingService billingService;

    @GetMapping
    public List<BillingResponse> myBills(@AuthenticationPrincipal PrincipalUser principal) {
        return billingService.myBills(principal);
    }
}
