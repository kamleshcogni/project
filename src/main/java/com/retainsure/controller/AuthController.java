package com.retainsure.controller;

import com.retainsure.dto.AuthRequest;
import com.retainsure.dto.AuthResponse;
import com.retainsure.service.AuthService;
import com.retainsure.service.AuditService;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;

    public AuthController(AuthService authService,
                          AuthenticationManager authenticationManager,
                          AuditService auditService) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.auditService = auditService;
    }
    @PermitAll
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        AuthResponse response = authService.login(request);
        auditService.logWithUsername(
                request.username(),
                "LOGIN",
                "User",
                String.valueOf(response.userId()),
                "User logged in"
        );
        return response;
    }
}