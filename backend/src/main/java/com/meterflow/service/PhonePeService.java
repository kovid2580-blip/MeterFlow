package com.meterflow.service;

import com.meterflow.dto.SubscriptionDtos.SubscriptionResponse;
import com.meterflow.entity.PlanType;
import com.meterflow.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhonePeService {
    private final RestTemplate restTemplate;

    @Value("${app.phonepe.environment:SANDBOX}")
    private String environment;

    @Value("${app.phonepe.client_id:}")
    private String clientId;

    @Value("${app.phonepe.client_version:}")
    private String clientVersion;

    @Value("${app.phonepe.client_secret:}")
    private String clientSecret;

    @Value("${app.phonepe.redirect_url:http://localhost:5173}")
    private String redirectUrl;

    @Value("${app.phonepe.pro_amount_paise:99900}")
    private long proAmountPaise;

    private String accessToken;
    private long accessTokenExpiresAt;

    public SubscriptionResponse createProPayment(User user, PlanType planType) {
        if (planType != PlanType.PRO) {
            throw new IllegalArgumentException("Only the PRO plan can be purchased");
        }

        String merchantOrderId = "MF-" + UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> request = Map.of(
                "merchantOrderId", merchantOrderId,
                "amount", proAmountPaise,
                "expireAfter", 1200,
                "paymentFlow", Map.of(
                        "type", "PG_CHECKOUT",
                        "message", "MeterFlow PRO plan",
                        "merchantUrls", Map.of("redirectUrl", redirectUrl)
                ),
                "disablePaymentRetry", true,
                "metaInfo", Map.of(
                        "udf1", user.getId(),
                        "udf2", user.getEmail(),
                        "udf3", planType.name()
                )
        );

        Map<?, ?> response = postJson(payUrl(), request);
        String orderId = stringValue(response.get("orderId"));
        String state = stringValue(response.get("state"));
        String phonePeRedirectUrl = stringValue(response.get("redirectUrl"));
        if (phonePeRedirectUrl == null || phonePeRedirectUrl.isBlank()) {
            throw new IllegalStateException("PhonePe did not return a checkout URL");
        }
        return new SubscriptionResponse(merchantOrderId, orderId, phonePeRedirectUrl, state, planType.name());
    }

    public String orderState(String merchantOrderId) {
        HttpHeaders headers = authHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            Map<?, ?> response = restTemplate.exchange(
                    statusUrl(merchantOrderId),
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    Map.class
            ).getBody();
            return response == null ? "UNKNOWN" : stringValue(response.get("state"));
        } catch (HttpStatusCodeException ex) {
            throw new IllegalStateException(errorMessage(ex));
        }
    }

    private Map<?, ?> postJson(String url, Map<String, Object> body) {
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, authHeaders());
        try {
            Map<?, ?> response = restTemplate.postForObject(url, entity, Map.class);
            if (response == null) {
                throw new IllegalStateException("PhonePe returned an empty response");
            }
            return response;
        } catch (HttpStatusCodeException ex) {
            throw new IllegalStateException(errorMessage(ex));
        }
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, tokenTypePrefix() + getAccessToken());
        return headers;
    }

    private synchronized String getAccessToken() {
        long now = System.currentTimeMillis() / 1000;
        if (accessToken != null && now < accessTokenExpiresAt - 60) {
            return accessToken;
        }
        ensureConfigured();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_version", clientVersion);
        form.add("client_secret", clientSecret);
        form.add("grant_type", "client_credentials");

        try {
            Map<?, ?> response = restTemplate.postForObject(authUrl(), new HttpEntity<>(form, headers), Map.class);
            if (response == null) {
                throw new IllegalStateException("PhonePe auth returned an empty response");
            }
            accessToken = stringValue(response.get("access_token"));
            Number expiresAt = (Number) response.get("expires_at");
            accessTokenExpiresAt = expiresAt == null ? now + 300 : expiresAt.longValue();
            if (accessToken == null || accessToken.isBlank()) {
                throw new IllegalStateException("PhonePe auth did not return an access token");
            }
            return accessToken;
        } catch (HttpStatusCodeException ex) {
            throw new IllegalStateException(errorMessage(ex));
        }
    }

    private void ensureConfigured() {
        if (clientId.isBlank() || clientVersion.isBlank() || clientSecret.isBlank()) {
            throw new IllegalStateException("PhonePe credentials are not configured");
        }
    }

    private String authUrl() {
        return isProduction()
                ? "https://api.phonepe.com/apis/identity-manager/v1/oauth/token"
                : "https://api-preprod.phonepe.com/apis/pg-sandbox/v1/oauth/token";
    }

    private String payUrl() {
        return isProduction()
                ? "https://api.phonepe.com/apis/pg/checkout/v2/pay"
                : "https://api-preprod.phonepe.com/apis/pg-sandbox/checkout/v2/pay";
    }

    private String statusUrl(String merchantOrderId) {
        String base = isProduction()
                ? "https://api.phonepe.com/apis/pg/checkout/v2/order/"
                : "https://api-preprod.phonepe.com/apis/pg-sandbox/checkout/v2/order/";
        return base + merchantOrderId + "/status?details=false";
    }

    private boolean isProduction() {
        return "PRODUCTION".equalsIgnoreCase(environment);
    }

    private String tokenTypePrefix() {
        return "O-Bearer ";
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String errorMessage(HttpStatusCodeException ex) {
        return ex.getResponseBodyAsString().isBlank()
                ? "PhonePe request failed with status " + ex.getStatusCode().value()
                : ex.getResponseBodyAsString();
    }
}
