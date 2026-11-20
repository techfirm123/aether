package com.aether.platform.identity.application;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Email or password is incorrect, or the account is not active.");
    }
}