CREATE TABLE roles(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(30) NOT NULL UNIQUE
);

CREATE INDEX idx_roles_name ON roles(name);
