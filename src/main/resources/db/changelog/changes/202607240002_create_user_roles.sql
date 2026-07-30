CREATE TABLE IF NOT EXISTS user_roles (
    id         BIGSERIAL PRIMARY KEY,
    user_id    UUID NOT NULL,
    role_id    BIGINT NOT NULL,

    CONSTRAINT uk_users_roles UNIQUE (user_id, role_id)
);
