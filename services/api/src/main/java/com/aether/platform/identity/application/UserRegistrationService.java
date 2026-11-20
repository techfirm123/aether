package com.aether.platform.identity.application;

import com.aether.platform.identity.infrastructure.JdbcUserIdentityRepository;
import com.aether.platform.web.CorrelationIdFilter;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserRegistrationService {

    private final JdbcUserIdentityRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService tokenService;
    private final VerificationEmailSender emailSender;
    private final Clock clock;
    private final Duration verificationTokenTtl;

    public UserRegistrationService(
            JdbcUserIdentityRepository repository,
            PasswordEncoder passwordEncoder,
            VerificationTokenService tokenService,
            VerificationEmailSender emailSender,
            Clock clock,
            @Value("${aether.identity.verification-token-ttl:PT24H}") Duration verificationTokenTtl) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.emailSender = emailSender;
        this.clock = clock;
        this.verificationTokenTtl = verificationTokenTtl;
    }

    @Transactional
    public void register(String requestedEmail, String requestedDisplayName, String rawPassword) {
        String email = requestedEmail.strip().toLowerCase(Locale.ROOT);
        String displayName = requestedDisplayName.strip();

        // Perform the adaptive hash before the existence check to reduce
        // timing differences. Public rate limiting is still required before
        // this endpoint is exposed beyond a controlled environment.
        String passwordHash = passwordEncoder.encode(rawPassword);

        if (repository.emailExists(email)) {
            return;
        }

        Instant now = clock.instant();
        UUID userId = UUID.randomUUID();
        String rawToken = tokenService.generateToken();
        String tokenHash = tokenService.hashToken(rawToken);
        Instant expiresAt = now.plus(verificationTokenTtl);

        repository.insertPendingUser(
                userId,
                email,
                displayName,
                passwordHash,
                now);
        repository.insertEmailVerificationToken(
                UUID.randomUUID(),
                userId,
                tokenHash,
                now,
                expiresAt);
        repository.insertAuditEvent(
                UUID.randomUUID(),
                userId,
                "USER_REGISTRATION_REQUESTED",
                MDC.get(CorrelationIdFilter.MDC_KEY),
                now);

        // The sender is selected by runtime profile. Local development logs
        // the one-time token; production sends it through configured SMTP.
        emailSender.sendVerificationToken(email, displayName, rawToken);
    }
}