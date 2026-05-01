package com.meterflow.controller;

import com.meterflow.dto.ApiDtos.ApiKeyResponse;
import com.meterflow.dto.ApiDtos.ApiResponse;
import com.meterflow.dto.ApiDtos.CreateApiRequest;
import com.meterflow.dto.ApiDtos.GenerateKeyRequest;
import com.meterflow.security.PrincipalUser;
import com.meterflow.service.ApiManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ApiController {
    private final ApiManagementService apiManagementService;

    @PostMapping("/api/create")
    public ApiResponse create(@Valid @RequestBody CreateApiRequest request,
                              @AuthenticationPrincipal PrincipalUser principal) {
        return apiManagementService.create(request, principal);
    }

    @GetMapping("/api/myapis")
    public List<ApiResponse> myApis(@AuthenticationPrincipal PrincipalUser principal) {
        return apiManagementService.myApis(principal);
    }

    @DeleteMapping("/api/{id}")
    public void delete(@PathVariable String id, @AuthenticationPrincipal PrincipalUser principal) {
        apiManagementService.delete(id, principal);
    }

    @PostMapping("/apikey/generate/{apiId}")
    public ApiKeyResponse generate(@PathVariable String apiId,
                                   @RequestBody(required = false) GenerateKeyRequest request,
                                   @AuthenticationPrincipal PrincipalUser principal) {
        return apiManagementService.generateKey(apiId, request == null ? null : request.planType(), principal);
    }

    @GetMapping("/apikey/mykeys")
    public List<ApiKeyResponse> myKeys(@AuthenticationPrincipal PrincipalUser principal) {
        return apiManagementService.myKeys(principal);
    }

    @PostMapping("/apikey/revoke/{id}")
    public ApiKeyResponse revoke(@PathVariable String id, @AuthenticationPrincipal PrincipalUser principal) {
        return apiManagementService.revoke(id, principal);
    }

    @PostMapping("/apikey/rotate/{id}")
    public ApiKeyResponse rotate(@PathVariable String id, @AuthenticationPrincipal PrincipalUser principal) {
        return apiManagementService.rotate(id, principal);
    }
}
