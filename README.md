# Core Authentication microservice

## About The Project

The microservice application to manage user access abd refresh tokens.

## Environment variables

| Variable                   | Type    | Desription                                                         |
|----------------------------|---------|--------------------------------------------------------------------| 
| `AUTH_SERVICE_PORT`        | String  | Application post                                                   |
| `AUTH_SERVICE_DB_NAME`     | String  | Database schema name                                               |
| `AUTH_SERVICE_DB_PORT`     | String  | Database port                                                      |
| `AUTH_SERVICE_DB_USERNAME` | String  | Database user name                                                 |
| `AUTH_SERVICE_DB_PASSWORD` | String  | Database user password                                             |
| `SHOW_SQL`                 | Boolean | Not mandatory parameter to allow show sql queries in debug only    |
| `CARD_SECRET_KEY`          | Boolean | 32 symblos secret password to encrypt/dercypt payment card numbers |
| `REDIS_SECRET_PASSWORD`    | String  | Redis password                                                     |
| `KAFKA_HOST_POST`          | String  | Kafka host:port                                                    |

## Tables

### Table `user_credentials`

| Column            | Type         | Constraints |
|-------------------|--------------|-------------| 
| `user_id`         | UUID         | NOT NULL    |
| `first_name`      | VARCHAR(50)  | NOT NULL    |
| `last_name`       | VARCHAR(50)  | NOT NULL    |
| `birth_date`      | DATE         | NOT NULL    |
| `email`           | VARCHAR(100) | NOT NULL    |
| `password_hash`   | VARCHAR(100) | NOT NULL    |
| `password_active` | BOOLEAN      | NOT NULL    |
| `active`          | BOOLEAN      | NOT NULL    |
| `created_at`      | TIMESTAMP    | NOT NULL    |
| `updated_at`      | TIMESTAMP    | NOT NULL    |


### Table `roles`

| Column   | Type          | Constraints           |
|----------|---------------|-----------------------| 
| `id`     | BIGSERIAL     | NOT NULL AUTOINCREMET |
| `name`   | VARCHAR(30)   | NOT NULL UNIQUE       |

Indexes

* idx_roles_name role(name)

### Table `user_credential_roles`

| Column    | Type      | Constraints           |
|-----------|-----------|-----------------------| 
| `id`      | BIGSERIAL | NOT NULL AUTOINCREMET |
| `user_id` | UUID      | NOT NULL              |
| `role_id` | BIGINT    | NOT NULL              |

CONSTRAINT uk_user_credentials_roles UNIQUE (user_id, role_id)


### Table `refresh_tokens`

| Column            | Type         | Constraints |
|-------------------|--------------|-------------| 
| `user_id`         | UUID         | NOT NULL    |
| `session_id`      | UUID         | NOT NULL    |
| `token_hash`      | VARCHAR(64)  | NOT NULL    |
| `created_at`      | TIMESTAMP    | NOT NULL    |
| `updated_at`      | TIMESTAMP    | NOT NULL    |

CONSTRAINT pk_refresh_tokens PRIMARY KEY (user_id, session_id)

Indexes

* UNIQUE idx_refresh_tokens_token_hash  refresh_tokens(token_hash)
* idx_refresh_tokens_expiry_date        refresh_tokens(expiry_date)
