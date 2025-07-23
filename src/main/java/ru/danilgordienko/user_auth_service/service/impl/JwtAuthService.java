package ru.danilgordienko.user_auth_service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
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
import ru.danilgordienko.user_auth_service.exception.TokenAlreadyRevokedException;
import ru.danilgordienko.user_auth_service.exception.TokenExpiredException;
import ru.danilgordienko.user_auth_service.exception.TokenNotFoundException;
import ru.danilgordienko.user_auth_service.exception.UserNotFoundException;
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


    // регисрация админа по умолчанию
    @PostConstruct
    public void initAdmin() {
        String defaultAdminLogin = "admin";

        boolean adminExists = userRepository.existsByLogin(defaultAdminLogin);
        if (!adminExists) {
            User admin = User.builder()
                    .login(defaultAdminLogin)
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(Set.of(Role.ADMIN.name()))
                    .build();
            userRepository.save(admin);
            System.out.println("Default admin user created: " + defaultAdminLogin);
        }
    }


    // аунтификация пользователя по учетным данным
    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Attempting login for user: {}", loginRequest.getLogin());
        // аунтфицируем по логину и паролю
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    loginRequest.getLogin(),
                    loginRequest.getPassword()
            )
        );
        var user = getUserByLogin(loginRequest.getLogin());
;        // генерируем токены
        var accessToken = jwtService.generateAccessToken(UserDetailsImpl.build(user));
        var  refreshToken = jwtService.generateRefreshToken(UserDetailsImpl.build(user));
        // удаляем все старые токены юзера
        refreshTokenRepository.deleteAllByUser(user);
        // сохраняем refresh токен
        saveRefreshToken(refreshToken, user);
        return new  AuthResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        log.info("Registering new user: {}", registerRequest.getLogin());
        User user = User.builder()
                .email(registerRequest.getEmail())
                .login(registerRequest.getLogin())
                .password(passwordEncoder.encode(registerRequest.getPassword())) // кодируем пароль
                .roles(Set.of(Role.GUEST.name()))
                .build();
        userRepository.save(user);
        // генерируем токены
        var accessToken = jwtService.generateAccessToken(UserDetailsImpl.build(user));
        var  refreshToken = jwtService.generateRefreshToken(UserDetailsImpl.build(user));
        // сохраняем refresh токен
        saveRefreshToken(refreshToken, user);
        return new  AuthResponse(accessToken, refreshToken);
    }

    // сохраняет токен в бд
    private void saveRefreshToken(String refreshToken, User user)
    {
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .token(refreshToken)
                        .user(user)
                        .build()
        );
    }

    // получение токена из http заголовка
    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        // Проверяем, что заголовок начинается с "Bearer " и извлекаем сам токен
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    // проверки что токен корректный
    private void validateToken(String token) {
        var storedToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException("Invalid refresh token"));
        // не просрочен
        if (jwtService.isTokenExpired(token)) {
            throw new TokenExpiredException("Refresh token is expired");
        }
        //не отозван
        if (storedToken.isRevoked()) {
            throw new TokenAlreadyRevokedException("Token already revoked");
        }
    }

    // получение нового access токена
    @Override
    public void refresh(HttpServletRequest request, HttpServletResponse  response) throws IOException {
        log.info("Attempting to refresh access token using refresh token");
        String refreshToken = getTokenFromRequest(request);

        // Извлекаем имя пользователя из токена
        String username = jwtService.getLoginFromToken(refreshToken);
        log.debug("Extracted username from refresh token: {}", username);

        // Загружаем данные пользователя по логину
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        validateToken(refreshToken);

        var accessToken = jwtService.generateAccessToken(userDetails);
        var authResponse = new AuthResponse(accessToken, refreshToken);
        log.info("Access token refreshed for user: {}", username);

        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
    }

    // отзыв refresh токена
    @Override
    @Transactional
    public void logout(HttpServletRequest request) {
        log.info("Attempting logout with refresh token");
        String refreshToken = getTokenFromRequest(request);

        validateToken(refreshToken);

        revokeToken(refreshToken);
        log.info("Refresh token revoked: {}", refreshToken);
    }

    // помечает refresh токен у пользователя как отозванный
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    // получение пользователя по логину
    private User getUserByLogin(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", login);
                    return  new UserNotFoundException("User " +
                            login +
                            "not found");
                });
    }

    @Transactional
    @Override
    public void addAdminRole(String login) {
        var user = getUserByLogin(login);
        if (user.getRoles().contains(Role.ADMIN.name())) {
            throw new IllegalStateException("User "+login+" is already an administrator");
        }
        user.getRoles().add(Role.ADMIN.name());
        userRepository.save(user);
    }

    @Transactional
    public void assignPremiumRole(String login) {
        User user = getUserByLogin(login);
        if (user.getRoles().contains(Role.PREMIUM_USER.name())) {
            throw new IllegalStateException("User "+login+" is already an premium");
        }

        user.getRoles().add(Role.PREMIUM_USER.name());
        userRepository.save(user);
    }


}
