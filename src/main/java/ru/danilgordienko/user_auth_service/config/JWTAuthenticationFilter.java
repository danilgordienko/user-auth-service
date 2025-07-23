package ru.danilgordienko.user_auth_service.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            log.info("JWT Authentication Filter Started");
            // Получаем JWT-токен из запроса
            String token = getTokenFromRequest(request);

            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null ) {
                // Извлекаем имя пользователя из токена
                String username = jwtService.getLoginFromToken(token);
                log.info("Получен токен для пользователя: {}", username);

                // Загружаем данные пользователя по логину
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (!jwtService.isTokenExpired(token)) {
                    // Создаем объект аутентификации и устанавливаем его в SecurityContext
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("Аутентификация пользователя '{}' успешно установлена", username);
                }
            }

        // Передаем управление следующему фильтру в цепочке
        filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.warn("JWT-токен просрочен: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "JWT-токен просрочен");
        } catch (UnsupportedJwtException | MalformedJwtException e) {
            log.warn("Недействительный JWT-токен: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Недействительный JWT-токен");
        } catch (JwtException e) {
            log.warn("Неверный JWT-токен: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Неверный JWT-токен");
        } catch (Exception e) {
            log.error("Ошибка аутентификации: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Ошибка аутентификации");
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        // Проверяем, что заголовок начинается с "Bearer " и извлекаем сам токен
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
