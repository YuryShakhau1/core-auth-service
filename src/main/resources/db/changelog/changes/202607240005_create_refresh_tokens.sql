CREATE TABLE IF NOT EXISTS refresh_tokens (
    user_id          UUID NOT NULL,
    session_id       UUID NOT NULL,
    token_hash       VARCHAR(64) NOT NULL,
    created_at       TIMESTAMP NOT NULL,
    expiry_date      TIMESTAMP NOT NULL,

    CONSTRAINT pk_refresh_tokens PRIMARY KEY (user_id, session_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expiry_date ON refresh_tokens(expiry_date);
