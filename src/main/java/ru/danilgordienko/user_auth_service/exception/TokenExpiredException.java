package ru.danilgordienko.user_auth_service.exception;

public class TokenExpiredException extends TokenException {
    public TokenExpiredException(String message){
        super(message);
    }
}
