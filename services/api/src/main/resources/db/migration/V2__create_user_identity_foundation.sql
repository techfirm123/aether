-- User identity and email verification foundation.
-- The application role can create and update account records but cannot
-- modify or delete immutable identity audit events.

CREATE TABLE aether.user_account (
                                     id UUID PRIMARY KEY,
                                     email VARCHAR(320) NOT NULL,
                                     display_name VARCHAR(80) NOT NULL,
                                     status VARCHAR(32) NOT NULL,
                                     email_verified_at TIMESTAMPTZ,
                                     created_at TIMESTAMPTZ NOT NULL,
                                     updated_at TIMESTAMPTZ NOT NULL,
                                     CONSTRAINT uk_user_account_email UNIQUE (email),
                                     CONSTRAINT ck_user_account_status CHECK (
                                         status IN ('PENDING_VERIFICATION', 'ACTIVE', 'SUSPENDED', 'CLOSED')
                                         )
);

CREATE TABLE aether.user_password_credential (
                                                 user_id UUID PRIMARY KEY,
                                                 password_hash VARCHAR(512) NOT NULL,
                                                 created_at TIMESTAMPTZ NOT NULL,
                                                 updated_at TIMESTAMPTZ NOT NULL,
                                                 CONSTRAINT fk_user_password_credential_user
                                                     FOREIGN KEY (user_id) REFERENCES aether.user_account (id)
);

CREATE TABLE aether.email_verification_token (
                                                 id UUID PRIMARY KEY,
                                                 user_id UUID NOT NULL,
                                                 token_hash VARCHAR(64) NOT NULL,
                                                 created_at TIMESTAMPTZ NOT NULL,
                                                 expires_at TIMESTAMPTZ NOT NULL,
                                                 consumed_at TIMESTAMPTZ,
                                                 CONSTRAINT uk_email_verification_token_hash UNIQUE (token_hash),
                                                 CONSTRAINT fk_email_verification_token_user
                                                     FOREIGN KEY (user_id) REFERENCES aether.user_account (id)
);

CREATE INDEX idx_email_verification_user_expiry
    ON aether.email_verification_token (user_id, expires_at);

CREATE TABLE aether.identity_audit_event (
                                             id UUID PRIMARY KEY,
                                             user_id UUID,
                                             event_type VARCHAR(64) NOT NULL,
                                             correlation_id VARCHAR(64),
                                             occurred_at TIMESTAMPTZ NOT NULL,
                                             CONSTRAINT fk_identity_audit_event_user
                                                 FOREIGN KEY (user_id) REFERENCES aether.user_account (id)
                                                     ON DELETE SET NULL
);

CREATE INDEX idx_identity_audit_user_time
    ON aether.identity_audit_event (user_id, occurred_at);

-- The API uses account closure and token consumption rather than physical
-- deletion. Deletion remains an operational/migration responsibility.
REVOKE DELETE ON TABLE
    aether.user_account,
    aether.user_password_credential,
    aether.email_verification_token
    FROM aether_app;

REVOKE UPDATE, DELETE ON TABLE aether.identity_audit_event FROM aether_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA aether
    REVOKE DELETE ON TABLES FROM aether_app;