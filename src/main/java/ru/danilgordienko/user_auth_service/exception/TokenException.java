package ru.danilgordienko.user_auth_service.exception;

public class TokenException extends RuntimeException {
    public TokenException(String message){
        super(message);
    }
}
