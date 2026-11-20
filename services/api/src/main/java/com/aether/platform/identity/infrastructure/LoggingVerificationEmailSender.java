package com.aether.platform.identity.infrastructure;

import com.aether.platform.identity.application.VerificationEmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LoggingVerificationEmailSender implements VerificationEmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingVerificationEmailSender.class);

    @Override
    public void sendVerificationToken(String emailAddress, String displayName, String token) {
        // This adapter is deliberately restricted to the local profile.
        // Never enable it in a shared or production environment.
        log.warn("LOCAL DEVELOPMENT ONLY — AETHER email verification token: {}", token);
    }
}