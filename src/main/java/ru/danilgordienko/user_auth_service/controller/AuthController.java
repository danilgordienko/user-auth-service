package ru.danilgordienko.user_auth_service.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.danilgordienko.user_auth_service.dto.AuthResponse;
import ru.danilgordienko.user_auth_service.dto.LoginRequest;
import ru.danilgordienko.user_auth_service.dto.RegisterRequest;
import ru.danilgordienko.user_auth_service.service.AuthService;

import java.io.IOException;
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest registerRequest) {
        log.info("Received registration request for login: {}", registerRequest.getLogin());
        var response = authService.register(registerRequest);
        log.info("Registration successful for login: {}", registerRequest.getLogin());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        log.info("Received login request for login: {}", loginRequest.getLogin());
        var response = authService.login(loginRequest);
        log.info("Login successful for login: {}", loginRequest.getLogin());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public void refresh(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("Received token refresh request");
        authService.refresh(request, response);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        log.info("Received logout request");
        authService.logout(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add/admin/{login}")
    public ResponseEntity<String> assignAdminRole(@PathVariable String login) {
        authService.addAdminRole(login);
        return ResponseEntity.ok("Admin role assigned to user " + login);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add/premium/{login}")
    public ResponseEntity<String> assignPremiumRole(@PathVariable String login) {
        authService.assignPremiumRole(login);
        return ResponseEntity.ok("Premium role assigned to " + login);
    }
}

