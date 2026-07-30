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

## Local debug

Before debugging create .env file with project properties.

The property file example:

AUTH_SERVICE_PORT=8081

AUTH_SERVICE_DB_NAME=user_db
AUTH_SERVICE_DB_PORT=5433
AUTH_SERVICE_DB_USERNAME=db_username
AUTH_SERVICE_DB_PASSWORD=db_password

KAFKA_HOST_POST=localhost:9092

SHOW_SQL=true

JWT_SECRET=VsGzyl4BZTe841l3jPUpVjuFrEabuERfimDlBAq3XB7YyOYl9pWCPmNsAg8IdgGP
REDIS_SECRET_PASSWORD=redis_password

ADMIN_INIT_SECRET=Admin_init_secret

## Application docker-compose

docker-compose example

```yaml
services:
  auth-service-db:
    image: postgres:18-alpine
    container_name: auth_service_postgres
    environment:
      - POSTGRES_DB=${AUTH_SERVICE_DB_NAME}
      - POSTGRES_USER=${AUTH_SERVICE_DB_USERNAME}
      - POSTGRES_PASSWORD=${AUTH_SERVICE_DB_PASSWORD}
      - PGDATA=/var/lib/postgresql/data/pgdata
    ports:
      - "${AUTH_SERVICE_DB_PORT}:5432"
    volumes:
      - auth_service_postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U $$POSTGRES_USER -d $$POSTGRES_DB"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: always

volumes:
  auth_service_postgres_data:
```

##  REST endpoints

The base URL for all API endpoints.

## User REST endpoints

---

### 1. Get current user info.

GET /auth/users/me  
`Authorization: Bearer <your_access_token>`  

* **Success (201 Created):**
```json
{
  "id": "<user_id_uuid>",
  "firstName": "<user_first_name>",
  "lastName": "<user_last_name>",
  "birthDate": "<user_birth_date> in yyyy-MM-dd format",
  "email": "<user_email>",
  "active": "<true_or_false_user_status>"
}
```

---

### 2. Get current user roles.

GET /auth/users/me/roles  
`Authorization: Bearer <your_access_token>`  

* **Success (201 Created):**
```json
{
  "roleNames": [ "<user_role_name>" ]
}
```

---

### 3. Create user.

Rest endpoint to register new user.

POST /auth/users  

```json
{
  "firstName": "<user_first_name>",
  "lastName": "<user_last_name>",
  "birthDate": "<user_birth_date> in yyyy-MM-dd format",
  "email": "<user_email>",
  "password": "<new_user_password>",
  "repeatPassword": "<new_user_password>",
  "active": "<true_or_false_user_status>"
}
```

* **Success (201 Created):**
```json
{
  "id": "<user_id_uuid>",
  "firstName": "<user_first_name>",
  "lastName": "<user_last_name>",
  "birthDate": "<user_birth_date> in yyyy-MM-dd format",
  "email": "<user_email>",
  "active": "<true_or_false_user_status>"
}
```

---

### 4. Create administrator.

Rest endpoint to register new administrator.

POST /auth/users/create-admin  

```json
{
  "firstName": "<user_first_name>",
  "lastName": "<user_last_name>",
  "birthDate": "<user_birth_date> in yyyy-MM-dd format",
  "email": "<user_email>",
  "password": "<new_user_password>",
  "repeatPassword": "<new_user_password>",
  "active": "<true_or_false_user_status>",
  "adminInitSecret": "<admin_secret>"
}
```

* **Success (201 Created):**
```json
{
  "id": "<user_id_uuid>",
  "firstName": "<user_first_name>",
  "lastName": "<user_last_name>",
  "birthDate": "<user_birth_date> in yyyy-MM-dd format",
  "email": "<user_email>",
  "active": "<true_or_false_user_status>"
}
```

---

### 5. Change user password.

Rest endpoint to change user password.
User must change password if he was created by administrator.

POST /auth/users/change-password  

```json
{
  "email": "<user_email>",
  "password": "<user_password>",
  "repeatPassword": "<user_password>",
  "newPassword": "<new_user_password>"
}
```

* **Success (200 Created):**

---

## Auth REST endpoints

---

### 1. Get current user info.

GET /auth/login  

```json
{
  "email": "<user_email>",
  "password": "<user_password>"
}
```

* **Success (200):**
```json
{
  "accessToken": "<access_token>",
  "refreshToken": "<refresh_token>"
}
```

---

### 2. Refresh token.

POST /auth/token/refresh  

```json
{
  "refreshToken": "<refresh_token>"
}
```

* **Success (200):**
```json
{
  "accessToken": "<access_token>",
  "refreshToken": "<refresh_token>"
}
```

---

### 3. Check if token valid.

GET /auth/token/{token}/valid  
`Authorization: Bearer <your_access_token>`  

* **Success (200):**
```json
{
  "valid": "<token_valid_status>"
}
```

---

### 4. Logout user.

Removes current session refresh token

POST /auth/logout  
`Authorization: Bearer <your_access_token>`  

* **Success (200):**

---

### 4. Logout user.

Removes all session refresh token

POST /auth/logout/all  
`Authorization: Bearer <your_access_token>`  

* **Success (200):**

---

## Role REST endpoints

---

### 1. Get current user info.

Returns all available roles.

GET /auth/roles  
`Authorization: Bearer <your_access_token>` with ADMIN role  

* **Success (200):**
```json
{
  "roleNames": [ "<user_role_name>" ]
}
```

---

## User role REST endpoints

---

### 1. Get user roles.

Returns all available roles.

GET /auth/users/{userId}/roles  
`Authorization: Bearer <your_access_token>` with ADMIN role  

* **Success (200):**
```json
{
  "roleNames": [ "<user_role_name>" ]
}
```

---

### 2. Add roles to user.

POST /auth/users/{userId}/roles  
`Authorization: Bearer <your_access_token>` with ADMIN role  

```json
{
  "roleNames": [ "<user_role_name>" ]
}
```

* **Success (200):**

---

### 3. Delete user role by role name.

DELETE /auth/users/{userId}/roles/{roleName}  
`Authorization: Bearer <your_access_token>` with ADMIN role  

* **Success (200):**

---
