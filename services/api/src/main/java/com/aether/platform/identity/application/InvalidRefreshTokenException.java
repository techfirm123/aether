package com.aether.platform.identity.application;

public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("The refresh token is invalid, expired or has been revoked.");
    }
}