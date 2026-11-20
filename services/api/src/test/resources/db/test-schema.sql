CREATE SCHEMA IF NOT EXISTS aether;

CREATE TABLE IF NOT EXISTS aether.user_account (
                                                   id UUID PRIMARY KEY,
                                                   email VARCHAR(320) NOT NULL UNIQUE,
    display_name VARCHAR(80) NOT NULL,
    status VARCHAR(32) NOT NULL,
    email_verified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                             CONSTRAINT ck_user_account_status CHECK (
                             status IN ('PENDING_VERIFICATION', 'ACTIVE', 'SUSPENDED', 'CLOSED')
    )
    );

CREATE TABLE IF NOT EXISTS aether.user_password_credential (
                                                               user_id UUID PRIMARY KEY,
                                                               password_hash VARCHAR(512) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                             CONSTRAINT fk_user_password_credential_user
                             FOREIGN KEY (user_id) REFERENCES aether.user_account (id)
    );

CREATE TABLE IF NOT EXISTS aether.email_verification_token (
                                                               id UUID PRIMARY KEY,
                                                               user_id UUID NOT NULL,
                                                               token_hash VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed_at TIMESTAMP WITH TIME ZONE,
                              CONSTRAINT fk_email_verification_token_user
                              FOREIGN KEY (user_id) REFERENCES aether.user_account (id)
    );

CREATE TABLE IF NOT EXISTS aether.identity_audit_event (
                                                           id UUID PRIMARY KEY,
                                                           user_id UUID,
                                                           event_type VARCHAR(64) NOT NULL,
    correlation_id VARCHAR(64),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
                              CONSTRAINT fk_identity_audit_event_user
                              FOREIGN KEY (user_id) REFERENCES aether.user_account (id)
                          ON DELETE SET NULL
    );