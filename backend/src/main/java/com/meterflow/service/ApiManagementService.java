package com.meterflow.service;

import com.meterflow.dto.ApiDtos.ApiKeyResponse;
import com.meterflow.dto.ApiDtos.ApiResponse;
import com.meterflow.dto.ApiDtos.CreateApiRequest;
import com.meterflow.entity.*;
import com.meterflow.repository.ApiKeyRepository;
import com.meterflow.repository.ApiProjectRepository;
import com.meterflow.security.PrincipalUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiManagementService {
    private final ApiProjectRepository apiProjectRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public ApiResponse create(CreateApiRequest request, PrincipalUser principal) {
        ApiProject api = ApiProject.builder()
                .user(principal.user())
                .name(request.name())
                .baseUrl(stripTrailingSlash(request.baseUrl()))
                .description(request.description())
                .build();
        return toResponse(apiProjectRepository.save(api));
    }

    public List<ApiResponse> myApis(PrincipalUser principal) {
        return apiProjectRepository.findByUser(principal.user()).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void delete(String id, PrincipalUser principal) {
        ApiProject api = ownedApi(id, principal);
        apiProjectRepository.delete(api);
    }

    public ApiKeyResponse generateKey(String apiId, PlanType planType, PrincipalUser principal) {
        ApiProject api = ownedApi(apiId, principal);
        ApiKey key = ApiKey.builder()
                .api(api)
                .keyValue("mf_" + randomToken())
                .status(ApiKeyStatus.ACTIVE)
                .planType(planType == null ? PlanType.FREE : planType)
                .build();
        return toKeyResponse(apiKeyRepository.save(key));
    }

    public List<ApiKeyResponse> myKeys(PrincipalUser principal) {
        return apiKeyRepository.findByApiUserId(principal.user().getId()).stream()
                .map(this::toKeyResponse)
                .toList();
    }

    public ApiKeyResponse revoke(String id, PrincipalUser principal) {
        ApiKey key = ownedKey(id, principal);
        key.setStatus(ApiKeyStatus.REVOKED);
        return toKeyResponse(apiKeyRepository.save(key));
    }

    public ApiKeyResponse rotate(String id, PrincipalUser principal) {
        ApiKey oldKey = ownedKey(id, principal);
        oldKey.setStatus(ApiKeyStatus.REVOKED);
        apiKeyRepository.save(oldKey);

        ApiKey replacement = ApiKey.builder()
                .api(oldKey.getApi())
                .keyValue("mf_" + randomToken())
                .status(ApiKeyStatus.ACTIVE)
                .planType(oldKey.getPlanType())
                .build();
        return toKeyResponse(apiKeyRepository.save(replacement));
    }

    private ApiProject ownedApi(String id, PrincipalUser principal) {
        ApiProject api = apiProjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("API not found"));
        if (!api.getUser().getId().equals(principal.user().getId())) {
            throw new AccessDeniedException("Not your API");
        }
        return api;
    }

    private ApiKey ownedKey(String id, PrincipalUser principal) {
        ApiKey key = apiKeyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("API key not found"));
        if (!key.getApi().getUser().getId().equals(principal.user().getId())) {
            throw new AccessDeniedException("Not your API key");
        }
        return key;
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private ApiResponse toResponse(ApiProject api) {
        return new ApiResponse(api.getId(), api.getName(), api.getBaseUrl(), api.getDescription(), api.getCreatedAt());
    }

    private ApiKeyResponse toKeyResponse(ApiKey key) {
        return new ApiKeyResponse(key.getId(), key.getApi().getId(), key.getKeyValue(), key.getStatus(), key.getPlanType(), key.getCreatedAt());
    }
}
