package com.aether.platform.identity.infrastructure;

import com.aether.platform.identity.domain.EmailVerificationToken;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcUserIdentityRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserIdentityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean emailExists(String normalizedEmail) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM aether.user_account WHERE email = ?",
                Integer.class,
                normalizedEmail);

        return count != null && count > 0;
    }

    public void insertPendingUser(
            UUID userId,
            String email,
            String displayName,
            String passwordHash,
            Instant now) {

        Timestamp timestamp = Timestamp.from(now);

        jdbcTemplate.update("""
                INSERT INTO aether.user_account (
                    id,
                    email,
                    display_name,
                    status,
                    email_verified_at,
                    created_at,
                    updated_at
                ) VALUES (?, ?, ?, 'PENDING_VERIFICATION', NULL, ?, ?)
                """,
                userId,
                email,
                displayName,
                timestamp,
                timestamp);

        jdbcTemplate.update("""
                INSERT INTO aether.user_password_credential (
                    user_id,
                    password_hash,
                    created_at,
                    updated_at
                ) VALUES (?, ?, ?, ?)
                """,
                userId,
                passwordHash,
                timestamp,
                timestamp);
    }

    public void insertEmailVerificationToken(
            UUID tokenId,
            UUID userId,
            String tokenHash,
            Instant createdAt,
            Instant expiresAt) {

        jdbcTemplate.update("""
                INSERT INTO aether.email_verification_token (
                    id,
                    user_id,
                    token_hash,
                    created_at,
                    expires_at,
                    consumed_at
                ) VALUES (?, ?, ?, ?, ?, NULL)
                """,
                tokenId,
                userId,
                tokenHash,
                Timestamp.from(createdAt),
                Timestamp.from(expiresAt));
    }

    public Optional<EmailVerificationToken> findUsableEmailVerificationToken(
            String tokenHash,
            Instant now) {

        List<EmailVerificationToken> tokens = jdbcTemplate.query("""
                SELECT id, user_id, expires_at
                FROM aether.email_verification_token
                WHERE token_hash = ?
                  AND consumed_at IS NULL
                  AND expires_at > ?
                """,
                (resultSet, rowNumber) -> new EmailVerificationToken(
                        resultSet.getObject("id", UUID.class),
                        resultSet.getObject("user_id", UUID.class),
                        resultSet.getTimestamp("expires_at").toInstant()),
                tokenHash,
                Timestamp.from(now));

        return tokens.stream().findFirst();
    }

    public boolean consumeEmailVerificationToken(UUID tokenId, Instant now) {
        int updated = jdbcTemplate.update("""
                UPDATE aether.email_verification_token
                SET consumed_at = ?
                WHERE id = ?
                  AND consumed_at IS NULL
                  AND expires_at > ?
                """,
                Timestamp.from(now),
                tokenId,
                Timestamp.from(now));

        return updated == 1;
    }

    public boolean activateUser(UUID userId, Instant now) {
        int updated = jdbcTemplate.update("""
                UPDATE aether.user_account
                SET status = 'ACTIVE',
                    email_verified_at = ?,
                    updated_at = ?
                WHERE id = ?
                  AND status = 'PENDING_VERIFICATION'
                """,
                Timestamp.from(now),
                Timestamp.from(now),
                userId);

        return updated == 1;
    }

    public void insertAuditEvent(
            UUID eventId,
            UUID userId,
            String eventType,
            String correlationId,
            Instant occurredAt) {

        jdbcTemplate.update("""
                INSERT INTO aether.identity_audit_event (
                    id,
                    user_id,
                    event_type,
                    correlation_id,
                    occurred_at
                ) VALUES (?, ?, ?, ?, ?)
                """,
                eventId,
                userId,
                eventType,
                correlationId,
                Timestamp.from(occurredAt));
    }
}