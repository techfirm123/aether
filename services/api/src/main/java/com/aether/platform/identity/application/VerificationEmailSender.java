package com.aether.platform.identity.application;

public interface VerificationEmailSender {

    void sendVerificationToken(String emailAddress, String displayName, String token);
}