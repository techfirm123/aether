package com.aether.platform.identity.infrastructure;

import com.aether.platform.identity.domain.AuthenticatedUserPrincipal;
import com.aether.platform.identity.domain.RefreshTokenRecord;
import com.aether.platform.identity.domain.UserSessionRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcSessionRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertSession(
            UUID sessionId,
            UUID userId,
            String accessTokenHash,
            Instant now,
            Instant accessExpiresAt,
            Instant sessionExpiresAt) {

        jdbcTemplate.update("""
                INSERT INTO aether.user_session (
                    id,
                    user_id,
                    created_at,
                    last_used_at,
                    expires_at,
                    access_token_hash,
                    access_token_expires_at,
                    revoked_at,
                    revocation_reason
                ) VALUES (?, ?, ?, ?, ?, ?, ?, NULL, NULL)
                """,
                sessionId,
                userId,
                Timestamp.from(now),
                Timestamp.from(now),
                Timestamp.from(sessionExpiresAt),
                accessTokenHash,
                Timestamp.from(accessExpiresAt));
    }

    public void insertRefreshToken(
            UUID tokenId,
            UUID sessionId,
            String tokenHash,
            Instant issuedAt,
            Instant expiresAt) {

        jdbcTemplate.update("""
                INSERT INTO aether.user_refresh_token (
                    id,
                    session_id,
                    token_hash,
                    issued_at,
                    expires_at,
                    consumed_at,
                    replaced_by_token_id
                ) VALUES (?, ?, ?, ?, ?, NULL, NULL)
                """,
                tokenId,
                sessionId,
                tokenHash,
                Timestamp.from(issuedAt),
                Timestamp.from(expiresAt));
    }

    public Optional<RefreshTokenRecord> findRefreshTokenForUpdate(
            String tokenHash) {

        List<RefreshTokenRecord> records = jdbcTemplate.query("""
                SELECT id, session_id, expires_at, consumed_at
                FROM aether.user_refresh_token
                WHERE token_hash = ?
                FOR UPDATE
                """,
                (resultSet, rowNumber) -> {
                    Timestamp consumedAt = resultSet.getTimestamp("consumed_at");

                    return new RefreshTokenRecord(
                            resultSet.getObject("id", UUID.class),
                            resultSet.getObject("session_id", UUID.class),
                            resultSet.getTimestamp("expires_at").toInstant(),
                            consumedAt == null ? null : consumedAt.toInstant());
                },
                tokenHash);

        return records.stream().findFirst();
    }

    public Optional<UserSessionRecord> findSessionForUpdate(UUID sessionId) {

        List<UserSessionRecord> sessions = jdbcTemplate.query("""
                SELECT
                    user_session.id,
                    user_session.user_id,
                    user_account.email,
                    user_account.display_name,
                    user_account.status AS account_status,
                    user_session.expires_at,
                    user_session.revoked_at
                FROM aether.user_session user_session
                JOIN aether.user_account user_account
                    ON user_account.id = user_session.user_id
                WHERE user_session.id = ?
                FOR UPDATE
                """,
                (resultSet, rowNumber) -> {
                    Timestamp revokedAt = resultSet.getTimestamp("revoked_at");

                    return new UserSessionRecord(
                            resultSet.getObject("id", UUID.class),
                            resultSet.getObject("user_id", UUID.class),
                            resultSet.getString("email"),
                            resultSet.getString("display_name"),
                            resultSet.getString("account_status"),
                            resultSet.getTimestamp("expires_at").toInstant(),
                            revokedAt == null ? null : revokedAt.toInstant());
                },
                sessionId);

        return sessions.stream().findFirst();
    }

    public boolean consumeRefreshToken(
            UUID tokenId,
            Instant now,
            UUID replacedByTokenId) {

        int updated = jdbcTemplate.update("""
                UPDATE aether.user_refresh_token
                SET consumed_at = ?,
                    replaced_by_token_id = ?
                WHERE id = ?
                  AND consumed_at IS NULL
                """,
                Timestamp.from(now),
                replacedByTokenId,
                tokenId);

        return updated == 1;
    }

    public boolean rotateSessionAccessToken(
            UUID sessionId,
            String accessTokenHash,
            Instant accessExpiresAt,
            Instant now) {

        int updated = jdbcTemplate.update("""
                UPDATE aether.user_session
                SET access_token_hash = ?,
                    access_token_expires_at = ?,
                    last_used_at = ?
                WHERE id = ?
                  AND revoked_at IS NULL
                """,
                accessTokenHash,
                Timestamp.from(accessExpiresAt),
                Timestamp.from(now),
                sessionId);

        return updated == 1;
    }

    public Optional<AuthenticatedUserPrincipal> findAuthenticatedUserByAccessTokenHash(
            String accessTokenHash,
            Instant now) {

        List<AuthenticatedUserPrincipal> principals = jdbcTemplate.query("""
                SELECT
                    user_session.user_id,
                    user_session.id AS session_id,
                    user_account.email,
                    user_account.display_name
                FROM aether.user_session user_session
                JOIN aether.user_account user_account
                    ON user_account.id = user_session.user_id
                WHERE user_session.access_token_hash = ?
                  AND user_session.access_token_expires_at > ?
                  AND user_session.expires_at > ?
                  AND user_session.revoked_at IS NULL
                  AND user_account.status = 'ACTIVE'
                """,
                (resultSet, rowNumber) -> new AuthenticatedUserPrincipal(
                        resultSet.getObject("user_id", UUID.class),
                        resultSet.getObject("session_id", UUID.class),
                        resultSet.getString("email"),
                        resultSet.getString("display_name")),
                accessTokenHash,
                Timestamp.from(now),
                Timestamp.from(now));

        return principals.stream().findFirst();
    }

    public boolean revokeSession(
            UUID sessionId,
            Instant now,
            String reason) {

        int updated = jdbcTemplate.update("""
                UPDATE aether.user_session
                SET revoked_at = ?,
                    revocation_reason = ?
                WHERE id = ?
                  AND revoked_at IS NULL
                """,
                Timestamp.from(now),
                reason,
                sessionId);

        return updated == 1;
    }
}