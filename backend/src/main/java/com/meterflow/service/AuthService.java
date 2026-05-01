package com.meterflow.service;

import com.meterflow.dto.AuthDtos.AuthResponse;
import com.meterflow.dto.AuthDtos.LoginRequest;
import com.meterflow.dto.AuthDtos.RegisterRequest;
import com.meterflow.entity.Role;
import com.meterflow.entity.User;
import com.meterflow.repository.UserRepository;
import com.meterflow.security.JwtService;
import com.meterflow.security.PrincipalUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role() == null ? Role.OWNER : request.role())
                .build();
        userRepository.save(user);
        return response(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        return response(user);
    }

    private AuthResponse response(User user) {
        PrincipalUser principal = new PrincipalUser(user);
        String token = jwtService.generateToken(principal, Map.of("role", user.getRole().name(), "userId", user.getId()));
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getPlanType());
    }
}
