package ru.danilgordienko.user_auth_service.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;
import ru.danilgordienko.user_auth_service.dto.AuthResponse;
import ru.danilgordienko.user_auth_service.dto.LoginRequest;
import ru.danilgordienko.user_auth_service.dto.RegisterRequest;

import java.io.IOException;

public interface AuthService {

    public AuthResponse login(LoginRequest loginRequest);
    public AuthResponse register(RegisterRequest registerRequest);
    public void refresh(HttpServletRequest request, HttpServletResponse response) throws IOException;
    public void logout(HttpServletRequest request);
}
