package com.meterflow.controller;

import com.meterflow.service.GatewayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

@RestController
@RequestMapping("/gateway")
@RequiredArgsConstructor
public class GatewayController {
    private final GatewayService gatewayService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @RequestMapping(value = "/{apiName}/**")
    public ResponseEntity<byte[]> proxy(@PathVariable String apiName,
                                        @RequestHeader("x-api-key") String apiKey,
                                        @RequestHeader HttpHeaders headers,
                                        @RequestBody(required = false) byte[] body,
                                        HttpServletRequest request) {
        String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String remaining = "/" + pathMatcher.extractPathWithinPattern(pattern, path);
        if (request.getQueryString() != null) {
            remaining += "?" + request.getQueryString();
        }
        return gatewayService.forward(apiName, remaining, apiKey, HttpMethod.valueOf(request.getMethod()), headers, body);
    }
}
