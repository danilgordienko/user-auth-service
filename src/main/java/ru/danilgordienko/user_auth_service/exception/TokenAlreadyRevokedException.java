package ru.danilgordienko.user_auth_service.exception;

public class TokenAlreadyRevokedException  extends TokenException{
    public TokenAlreadyRevokedException(String message){
        super(message);
    }
}
