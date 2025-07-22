package ru.danilgordienko.user_auth_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.danilgordienko.user_auth_service.model.User;
import ru.danilgordienko.user_auth_service.model.UserDetailsImpl;
import ru.danilgordienko.user_auth_service.repository.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        log.info("Поиск пользователя по логину: {}", login);
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> {
                    log.warn("Пользователь '{}' не найден", login);
                    return new UsernameNotFoundException(login + " not found");
                });

        log.info("Пользователь '{}' найден, возвращаем UserDetails", login);
        return UserDetailsImpl.build(user);
    }
}
