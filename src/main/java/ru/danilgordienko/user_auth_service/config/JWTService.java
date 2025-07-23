package ru.danilgordienko.user_auth_service.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.danilgordienko.user_auth_service.model.UserDetailsImpl;

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

    // генерирует access токен
    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(userDetails,  accessExpiration);
    }

    // генерирует refresh toker
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(userDetails,  refreshExpiration);
    }

     // Генерирует JWT-токен для аутентифицированного пользователя
    private String buildToken(UserDetails userDetails, int expiration) {
        UserDetailsImpl user = (UserDetailsImpl) userDetails;

        return Jwts.builder()
                .setSubject(user.getUsername()) // login
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .claim("id", user.getId())
                .claim("email", user.getEmail())
                .claim("roles", user.getAuthorities().stream()
                        .map(Object::toString)
                        .toList())
                .signWith(getSigningKey())
                .compact();
    }


    // проверка просрочен ли токен
    public boolean isTokenExpired(String token) {
        return getExpirationFromToken(token).before(new Date());
    }

    // получение ролей из токена
    public List<String> getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("roles", List.class);
    }

    // получение времени просрочки токена
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

    // Извлекает логин из переданного JWT-токена
    public String getLoginFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }
}
