package com.meterflow.controller;

import com.meterflow.dto.AuthDtos.AuthResponse;
import com.meterflow.dto.AuthDtos.LoginRequest;
import com.meterflow.dto.AuthDtos.RegisterRequest;
import com.meterflow.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
