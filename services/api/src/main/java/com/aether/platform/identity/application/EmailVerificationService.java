package com.aether.platform.identity.application;

import com.aether.platform.identity.domain.EmailVerificationToken;
import com.aether.platform.identity.infrastructure.JdbcUserIdentityRepository;
import com.aether.platform.web.CorrelationIdFilter;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailVerificationService {

    private final JdbcUserIdentityRepository repository;
    private final VerificationTokenService tokenService;
    private final Clock clock;

    public EmailVerificationService(
            JdbcUserIdentityRepository repository,
            VerificationTokenService tokenService,
            Clock clock) {
        this.repository = repository;
        this.tokenService = tokenService;
        this.clock = clock;
    }

    @Transactional
    public void verify(String rawToken) {
        Instant now = clock.instant();
        String tokenHash = tokenService.hashToken(rawToken);

        Optional<EmailVerificationToken> token = repository
                .findUsableEmailVerificationToken(tokenHash, now);

        if (token.isEmpty()) {
            throw new InvalidVerificationTokenException();
        }

        EmailVerificationToken verificationToken = token.get();

        if (!repository.consumeEmailVerificationToken(verificationToken.id(), now)) {
            throw new InvalidVerificationTokenException();
        }

        if (!repository.activateUser(verificationToken.userId(), now)) {
            throw new InvalidVerificationTokenException();
        }

        repository.insertAuditEvent(
                UUID.randomUUID(),
                verificationToken.userId(),
                "USER_EMAIL_VERIFIED",
                MDC.get(CorrelationIdFilter.MDC_KEY),
                now);
    }
}