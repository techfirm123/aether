package com.aether.platform;

import com.aether.platform.identity.application.VerificationEmailSender;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class AetherTestConfiguration {

    @Bean
    VerificationEmailSender verificationEmailSender() {
        return (emailAddress, displayName, token) -> {
            // No email delivery is required by the foundation tests.
        };
    }
}