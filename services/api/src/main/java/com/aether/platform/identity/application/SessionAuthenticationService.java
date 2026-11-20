package com.aether.platform.identity.application;

import com.aether.platform.identity.domain.AuthenticatedUserPrincipal;
import com.aether.platform.identity.domain.LoginAccount;
import com.aether.platform.identity.domain.RefreshTokenRecord;
import com.aether.platform.identity.domain.SessionTokens;
import com.aether.platform.identity.domain.UserSessionRecord;
import com.aether.platform.identity.infrastructure.JdbcAuthenticationRepository;
import com.aether.platform.identity.infrastructure.JdbcSessionRepository;
import com.aether.platform.identity.infrastructure.JdbcUserIdentityRepository;
import com.aether.platform.web.CorrelationIdFilter;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionAuthenticationService {

    private final JdbcAuthenticationRepository authenticationRepository;
    private final JdbcSessionRepository sessionRepository;
    private final JdbcUserIdentityRepository identityRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionTokenGenerator tokenGenerator;
    private final Clock clock;
    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;
    private final String dummyPasswordHash;

    public SessionAuthenticationService(
            JdbcAuthenticationRepository authenticationRepository,
            JdbcSessionRepository sessionRepository,
            JdbcUserIdentityRepository identityRepository,
            PasswordEncoder passwordEncoder,
            SessionTokenGenerator tokenGenerator,
            Clock clock,
            @Value("${aether.identity.access-token-ttl:PT10M}") Duration accessTokenTtl,
            @Value("${aether.identity.refresh-token-ttl:P30D}") Duration refreshTokenTtl) {

        this.authenticationRepository = authenticationRepository;
        this.sessionRepository = sessionRepository;
        this.identityRepository = identityRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenGenerator = tokenGenerator;
        this.clock = clock;
        this.accessTokenTtl = accessTokenTtl;
        this.refreshTokenTtl = refreshTokenTtl;

        // Used when an account does not exist, so unknown-email login
        // attempts still perform an adaptive password-hash comparison.
        this.dummyPasswordHash = passwordEncoder.encode(
                UUID.randomUUID().toString());
    }

    @Transactional
    public Optional<SessionTokens> login(String requestedEmail, String rawPassword) {

        String normalizedEmail = requestedEmail.strip().toLowerCase(Locale.ROOT);

        Optional<LoginAccount> accountOptional =
                authenticationRepository.findAccountForLogin(normalizedEmail);

        String storedPasswordHash = accountOptional
                .map(LoginAccount::passwordHash)
                .orElse(dummyPasswordHash);

        boolean passwordMatches = passwordEncoder.matches(
                rawPassword,
                storedPasswordHash);

        boolean accountCanAuthenticate = accountOptional
                .filter(account -> "ACTIVE".equals(account.accountStatus()))
                .isPresent();

        if (accountOptional.isEmpty()
                || !passwordMatches
                || !accountCanAuthenticate) {

            UUID userId = accountOptional
                    .map(LoginAccount::userId)
                    .orElse(null);

            identityRepository.insertAuditEvent(
                    UUID.randomUUID(),
                    userId,
                    "LOGIN_REJECTED",
                    currentCorrelationId(),
                    clock.instant());

            return Optional.empty();
        }

        LoginAccount account = accountOptional.orElseThrow();
        Instant now = clock.instant();

        UUID sessionId = UUID.randomUUID();

        Instant sessionExpiresAt = now.plus(refreshTokenTtl);
        Instant accessExpiresAt = min(
                now.plus(accessTokenTtl),
                sessionExpiresAt);

        String accessToken = tokenGenerator.generateAccessToken();
        String refreshToken = tokenGenerator.generateRefreshToken();

        sessionRepository.insertSession(
                sessionId,
                account.userId(),
                tokenGenerator.hashToken(accessToken),
                now,
                accessExpiresAt,
                sessionExpiresAt);

        sessionRepository.insertRefreshToken(
                UUID.randomUUID(),
                sessionId,
                tokenGenerator.hashToken(refreshToken),
                now,
                sessionExpiresAt);

        identityRepository.insertAuditEvent(
                UUID.randomUUID(),
                account.userId(),
                "LOGIN_SUCCEEDED",
                currentCorrelationId(),
                now);

        return Optional.of(new SessionTokens(
                accessToken,
                refreshToken,
                accessExpiresAt,
                sessionExpiresAt));
    }

    @Transactional
    public Optional<SessionTokens> refresh(String rawRefreshToken) {

        if (!tokenGenerator.isValidRefreshTokenFormat(rawRefreshToken)) {
            return Optional.empty();
        }

        Instant now = clock.instant();
        String refreshTokenHash = tokenGenerator.hashToken(rawRefreshToken);

        Optional<RefreshTokenRecord> tokenOptional =
                sessionRepository.findRefreshTokenForUpdate(refreshTokenHash);

        if (tokenOptional.isEmpty()) {
            return Optional.empty();
        }

        RefreshTokenRecord oldToken = tokenOptional.orElseThrow();

        Optional<UserSessionRecord> sessionOptional =
                sessionRepository.findSessionForUpdate(oldToken.sessionId());

        if (sessionOptional.isEmpty()) {
            return Optional.empty();
        }

        UserSessionRecord session = sessionOptional.orElseThrow();

        // A previously consumed token is a replay attempt.
        // Revoke the session while its absolute lifetime is still active.
        // Returning normally allows the transaction to commit the revocation.
        if (oldToken.consumedAt() != null) {

            if (session.revokedAt() == null
                    && session.expiresAt().isAfter(now)) {

                revokeAndAudit(
                        session,
                        now,
                        "REFRESH_TOKEN_REUSE",
                        "REFRESH_TOKEN_REUSE_DETECTED");
            }

            return Optional.empty();
        }

        if (!oldToken.expiresAt().isAfter(now)
                || !session.expiresAt().isAfter(now)
                || session.revokedAt() != null
                || !"ACTIVE".equals(session.accountStatus())) {

            if (session.revokedAt() == null
                    && !"ACTIVE".equals(session.accountStatus())) {

                revokeAndAudit(
                        session,
                        now,
                        "ACCOUNT_NOT_ACTIVE",
                        "SESSION_REVOKED");
            }

            return Optional.empty();
        }

        String newAccessToken = tokenGenerator.generateAccessToken();
        String newRefreshToken = tokenGenerator.generateRefreshToken();

        UUID newRefreshTokenId = UUID.randomUUID();

        Instant newAccessExpiresAt = min(
                now.plus(accessTokenTtl),
                session.expiresAt());

        // Insert replacement first because the old record's
        // replaced_by_token_id has a foreign-key reference to it.
        sessionRepository.insertRefreshToken(
                newRefreshTokenId,
                session.id(),
                tokenGenerator.hashToken(newRefreshToken),
                now,
                session.expiresAt());

        boolean oldTokenConsumed = sessionRepository.consumeRefreshToken(
                oldToken.id(),
                now,
                newRefreshTokenId);

        boolean accessTokenRotated = sessionRepository.rotateSessionAccessToken(
                session.id(),
                tokenGenerator.hashToken(newAccessToken),
                newAccessExpiresAt,
                now);

        if (!oldTokenConsumed || !accessTokenRotated) {
            throw new IllegalStateException(
                    "The authentication session could not be rotated.");
        }

        identityRepository.insertAuditEvent(
                UUID.randomUUID(),
                session.userId(),
                "SESSION_REFRESHED",
                currentCorrelationId(),
                now);

        return Optional.of(new SessionTokens(
                newAccessToken,
                newRefreshToken,
                newAccessExpiresAt,
                session.expiresAt()));
    }

    @Transactional(readOnly = true)
    public Optional<AuthenticatedUserPrincipal> authenticateAccessToken(
            String rawAccessToken) {

        if (!tokenGenerator.isValidAccessTokenFormat(rawAccessToken)) {
            return Optional.empty();
        }

        return sessionRepository.findAuthenticatedUserByAccessTokenHash(
                tokenGenerator.hashToken(rawAccessToken),
                clock.instant());
    }

    @Transactional
    public void logout(AuthenticatedUserPrincipal principal) {

        Instant now = clock.instant();

        boolean revoked = sessionRepository.revokeSession(
                principal.sessionId(),
                now,
                "USER_LOGOUT");

        if (revoked) {
            identityRepository.insertAuditEvent(
                    UUID.randomUUID(),
                    principal.userId(),
                    "SESSION_REVOKED",
                    currentCorrelationId(),
                    now);
        }
    }

    private void revokeAndAudit(
            UserSessionRecord session,
            Instant now,
            String reason,
            String eventType) {

        boolean revoked = sessionRepository.revokeSession(
                session.id(),
                now,
                reason);

        if (revoked) {
            identityRepository.insertAuditEvent(
                    UUID.randomUUID(),
                    session.userId(),
                    eventType,
                    currentCorrelationId(),
                    now);
        }
    }

    private Instant min(Instant first, Instant second) {
        return first.isBefore(second) ? first : second;
    }

    private String currentCorrelationId() {
        return MDC.get(CorrelationIdFilter.MDC_KEY);
    }
}