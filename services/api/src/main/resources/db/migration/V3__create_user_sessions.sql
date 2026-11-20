-- Server-managed authentication sessions.
-- Raw access and refresh tokens are never stored in PostgreSQL.

CREATE TABLE aether.user_session (
                                     id UUID PRIMARY KEY,
                                     user_id UUID NOT NULL,
                                     created_at TIMESTAMPTZ NOT NULL,
                                     last_used_at TIMESTAMPTZ NOT NULL,
                                     expires_at TIMESTAMPTZ NOT NULL,
                                     access_token_hash VARCHAR(64) NOT NULL,
                                     access_token_expires_at TIMESTAMPTZ NOT NULL,
                                     revoked_at TIMESTAMPTZ,
                                     revocation_reason VARCHAR(64),

                                     CONSTRAINT uk_user_session_access_token_hash
                                         UNIQUE (access_token_hash),

                                     CONSTRAINT fk_user_session_user
                                         FOREIGN KEY (user_id)
                                             REFERENCES aether.user_account (id),

                                     CONSTRAINT ck_user_session_expiry
                                         CHECK (expires_at > created_at),

                                     CONSTRAINT ck_user_session_access_expiry
                                         CHECK (access_token_expires_at <= expires_at)
);

CREATE INDEX idx_user_session_user
    ON aether.user_session (user_id, created_at);

CREATE INDEX idx_user_session_active
    ON aether.user_session (user_id, expires_at)
    WHERE revoked_at IS NULL;


CREATE TABLE aether.user_refresh_token (
                                           id UUID PRIMARY KEY,
                                           session_id UUID NOT NULL,
                                           token_hash VARCHAR(64) NOT NULL,
                                           issued_at TIMESTAMPTZ NOT NULL,
                                           expires_at TIMESTAMPTZ NOT NULL,
                                           consumed_at TIMESTAMPTZ,
                                           replaced_by_token_id UUID,

                                           CONSTRAINT uk_user_refresh_token_hash
                                               UNIQUE (token_hash),

                                           CONSTRAINT fk_user_refresh_token_session
                                               FOREIGN KEY (session_id)
                                                   REFERENCES aether.user_session (id),

                                           CONSTRAINT fk_user_refresh_token_replacement
                                               FOREIGN KEY (replaced_by_token_id)
                                                   REFERENCES aether.user_refresh_token (id),

                                           CONSTRAINT ck_user_refresh_token_expiry
                                               CHECK (expires_at > issued_at)
);

CREATE INDEX idx_user_refresh_token_session
    ON aether.user_refresh_token (session_id, issued_at);


-- Runtime application role may update session state and consume refresh
-- tokens, but cannot physically delete authentication history.

REVOKE DELETE ON TABLE
    aether.user_session,
    aether.user_refresh_token
    FROM aether_app;