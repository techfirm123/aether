package com.aether.platform.identity.application;

public class InvalidVerificationTokenException extends RuntimeException {

    public InvalidVerificationTokenException() {
        super("The verification token is invalid or has expired.");
    }
}