package ru.danilgordienko.user_auth_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.danilgordienko.user_auth_service.model.RefreshToken;
import ru.danilgordienko.user_auth_service.model.User;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteAllByUser(User user);
}
