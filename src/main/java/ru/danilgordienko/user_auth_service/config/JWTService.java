package ru.danilgordienko.user_auth_service.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Service
public class JWTService {
    // Секретный ключ для подписи JWT
    @Value("${app.security.secret}")
    private String secret;
    // Время жизни токена
    @Value("${app.security.access-expiration}")
    private int accessExpiration;

    @Value("${app.security.refresh-expiration}")
    private int refreshExpiration;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(userDetails,  accessExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(userDetails,  refreshExpiration);
    }

    /**
     * Генерирует JWT-токен для аутентифицированного пользователя
     */
    private String buildToken(UserDetails userDetails, int expiration) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername()) // Устанавливаем имя пользователя в токен
                .setIssuedAt(new Date()) // Устанавливаем дату создания токена
                .claim("roles", userDetails.getAuthorities().stream()
                        .map(Object::toString)
                        .toList())
                .setExpiration(new Date(new Date().getTime() + expiration)) // Устанавливаем дату истечения
                .signWith(getSigningKey()) // Подписываем токен секретным ключом
                .compact();
    }

    public boolean isTokenExpired(String token) {
        return getExpirationFromToken(token).before(new Date());
    }

    public List<String> getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("roles", List.class);
    }


    private Date getExpirationFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Извлекает имя пользователя из переданного JWT-токена
     */
    public String getLoginFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }
}
