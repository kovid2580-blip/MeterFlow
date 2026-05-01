package com.meterflow.dto;

import com.meterflow.entity.Role;
import com.meterflow.entity.PlanType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDtos {
    public record RegisterRequest(@NotBlank String name, @Email String email, @NotBlank String password, Role role) {}
    public record LoginRequest(@Email String email, @NotBlank String password) {}
    public record AuthResponse(String token, String userId, String name, String email, Role role, PlanType planType) {}
}
