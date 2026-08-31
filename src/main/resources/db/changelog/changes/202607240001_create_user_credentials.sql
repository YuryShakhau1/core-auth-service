CREATE TABLE IF NOT EXISTS user_credentials (
    user_id          UUID PRIMARY KEY,
    first_name       VARCHAR(50) NOT NULL,
    last_name        VARCHAR(50) NOT NULL,
    email            VARCHAR(100) NOT NULL UNIQUE,
    password_hash    VARCHAR(100) NOT NULL,
    password_active  BOOLEAN NOT NULL,
    active           BOOLEAN NOT NULL,
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP NOT NULL
);
