package ru.danilgordienko.user_auth_service.exception;

public class TokenNotFoundException extends TokenException {
    public TokenNotFoundException(String message) {
        super(message);
    }
}
