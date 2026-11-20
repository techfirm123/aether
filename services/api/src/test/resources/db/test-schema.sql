CREATE SCHEMA IF NOT EXISTS aether;

-- User account
CREATE TABLE IF NOT EXISTS aether.user_account (
                                                   id UUID PRIMARY KEY,
                                                   email VARCHAR(320) NOT NULL UNIQUE,
    display_name VARCHAR(80) NOT NULL,
    status VARCHAR(32) NOT NULL,
    email_verified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                             CONSTRAINT ck_user_account_status CHECK (
                             status IN (
                             'PENDING_VERIFICATION',
                             'ACTIVE',
                             'SUSPENDED',
                             'CLOSED'
                             )
    )
    );

-- Password credentials
CREATE TABLE IF NOT EXISTS aether.user_password_credential (
                                                               user_id UUID PRIMARY KEY,
                                                               password_hash VARCHAR(512) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                             CONSTRAINT fk_user_password_credential_user
                             FOREIGN KEY (user_id)
    REFERENCES aether.user_account (id)
    );

-- Email verification tokens
CREATE TABLE IF NOT EXISTS aether.email_verification_token (
                                                               id UUID PRIMARY KEY,
                                                               user_id UUID NOT NULL,
                                                               token_hash VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed_at TIMESTAMP WITH TIME ZONE,
                              CONSTRAINT fk_email_verification_token_user
                              FOREIGN KEY (user_id)
    REFERENCES aether.user_account (id)
    );

-- Identity audit events
CREATE TABLE IF NOT EXISTS aether.identity_audit_event (
                                                           id UUID PRIMARY KEY,
                                                           user_id UUID,
                                                           event_type VARCHAR(64) NOT NULL,
    correlation_id VARCHAR(64),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
                              CONSTRAINT fk_identity_audit_event_user
                              FOREIGN KEY (user_id)
    REFERENCES aether.user_account (id)
                          ON DELETE SET NULL
    );

-- Authentication sessions
CREATE TABLE IF NOT EXISTS aether.user_session (
                                                   id UUID PRIMARY KEY,
                                                   user_id UUID NOT NULL,
                                                   created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                                   last_used_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                                   expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                                   access_token_hash VARCHAR(64) NOT NULL UNIQUE,
    access_token_expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
                             revocation_reason VARCHAR(64),
    CONSTRAINT fk_user_session_user
    FOREIGN KEY (user_id)
    REFERENCES aether.user_account (id),
    CONSTRAINT ck_user_session_expiry
    CHECK (expires_at > created_at),
    CONSTRAINT ck_user_session_access_expiry
    CHECK (access_token_expires_at <= expires_at)
    );

CREATE INDEX IF NOT EXISTS idx_user_session_user
    ON aether.user_session (user_id, created_at);

-- H2-compatible regular index.
-- Production PostgreSQL migration uses a partial index instead.
CREATE INDEX IF NOT EXISTS idx_user_session_active
    ON aether.user_session (user_id, expires_at, revoked_at);

-- Refresh-token history
CREATE TABLE IF NOT EXISTS aether.user_refresh_token (
                                                         id UUID PRIMARY KEY,
                                                         session_id UUID NOT NULL,
                                                         token_hash VARCHAR(64) NOT NULL UNIQUE,
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed_at TIMESTAMP WITH TIME ZONE,
                              replaced_by_token_id UUID,
                              CONSTRAINT fk_user_refresh_token_session
                              FOREIGN KEY (session_id)
    REFERENCES aether.user_session (id),
    CONSTRAINT fk_user_refresh_token_replacement
    FOREIGN KEY (replaced_by_token_id)
    REFERENCES aether.user_refresh_token (id),
    CONSTRAINT ck_user_refresh_token_expiry
    CHECK (expires_at > issued_at)
    );

CREATE INDEX IF NOT EXISTS idx_user_refresh_token_session
    ON aether.user_refresh_token (session_id, issued_at);