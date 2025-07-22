package ru.danilgordienko.user_auth_service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.danilgordienko.user_auth_service.config.JWTService;
import ru.danilgordienko.user_auth_service.dto.AuthResponse;
import ru.danilgordienko.user_auth_service.dto.LoginRequest;
import ru.danilgordienko.user_auth_service.dto.RegisterRequest;
import ru.danilgordienko.user_auth_service.model.*;
import ru.danilgordienko.user_auth_service.repository.RefreshTokenRepository;
import ru.danilgordienko.user_auth_service.repository.UserRepository;
import ru.danilgordienko.user_auth_service.service.AuthService;

import java.io.IOException;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtAuthService implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserDetailsService userDetailsService;
    private final JWTService jwtService;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    loginRequest.getLogin(),
                    loginRequest.getPassword()
            )
        );
        var user = userRepository.findByLogin(loginRequest.getLogin())
                .orElseThrow();
        var accessToken = jwtService.generateAccessToken(UserDetailsImpl.build(user));
        var  refreshToken = jwtService.generateRefreshToken(UserDetailsImpl.build(user));
        refreshTokenRepository.deleteAllByUser(user);
        saveRefreshToken(refreshToken, user);
        return new  AuthResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        User user = User.builder()
                .email(registerRequest.getEmail())
                .login(registerRequest.getLogin())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .roles(Set.of(Role.GUEST.name()))
                .build();
        userRepository.save(user);
        var accessToken = jwtService.generateAccessToken(UserDetailsImpl.build(user));
        var  refreshToken = jwtService.generateRefreshToken(UserDetailsImpl.build(user));
        saveRefreshToken(refreshToken, user);
        return new  AuthResponse(accessToken, refreshToken);
    }

    private void saveRefreshToken(String refreshToken, User user)
    {
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .token(refreshToken)
                        .user(user)
                        .build()
        );
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        // Проверяем, что заголовок начинается с "Bearer " и извлекаем сам токен
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void validateToken(String token) {
        var storedToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (jwtService.isTokenExpired(token)) {
            throw new RuntimeException("Refresh token is expired");
        }

        if (storedToken.isRevoked()) {
            throw new RuntimeException("Token already revoked");
        }
    }

    @Override
    public void refresh(HttpServletRequest request, HttpServletResponse  response) throws IOException {
        String refreshToken = getTokenFromRequest(request);

        // Извлекаем имя пользователя из токена
        String username = jwtService.getLoginFromToken(refreshToken);

        // Загружаем данные пользователя по логину
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        validateToken(refreshToken);

        var accessToken = jwtService.generateAccessToken(userDetails);
        var authResponse = new AuthResponse(accessToken, refreshToken);

        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request) {
        String refreshToken = getTokenFromRequest(request);

        validateToken(refreshToken);

        revokeToken(refreshToken);
        log.info("Refresh token revoked: {}", refreshToken);
    }


    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

}
