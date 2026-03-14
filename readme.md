# AuthNexus

AuthNexus is a Spring Boot backend for authentication and user management.

## What is implemented

- Email/password register and login
- JWT access token + refresh token flow
- Refresh token rotation with DB persistence
- Google OAuth2 login
- GitHub OAuth2 login
- Swagger UI for API docs
- OpenAPI config via `APIDocConfig`
- User CRUD APIs

## Tech Stack

- Java (toolchain currently set to Java 25)
- Spring Boot 4
- Spring Security
- Spring Data JPA
- MySQL
- JJWT
- Lombok
- MapStruct
- Springdoc OpenAPI (Swagger UI)
- Gradle Wrapper

## Project Layout

```text
src/main/java/com/authnexus/centralapplication
├── config/        # SecurityConfig, APIDocConfig, appConstant
├── controller/    # REST controllers
├── domains/       # DTOs and entities
├── exception/     # Global exception handling
├── Helper/        # Utility helpers
├── Mapper/        # DTO <-> entity mapping
├── repository/    # JPA repositories
├── Security/      # JWT, OAuth2 success handler, cookie service, filters
└── services/      # Business logic
```

## Prerequisites

- JDK (matching Gradle toolchain)
- MySQL
- Git

## Quick Start

1) Clone and enter project

```powershell
git clone https://github.com/Monudhakad1/AuthNexus-backend
cd AuthNexus
```

2) Create database

```sql
CREATE DATABASE authnexus;
```

3) Update `src/main/resources/application-dev.properties`

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `security.jwt.secret`
- `spring.security.oauth2.client.registration.google.client-id`
- `spring.security.oauth2.client.registration.google.client-secret`
- `spring.security.oauth2.client.registration.github.client-id`
- `spring.security.oauth2.client.registration.github.client-secret`

4) Run app

```powershell
.\gradlew bootRun
```

Default active profile is `dev` (`src/main/resources/application.properties`).

Current ports:
- `dev`: `8083`
- `qa`: `8081`
- `prod`: `8089`

## Swagger UI

Open after app start:

```text
http://localhost:8083/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8083/v3/api-docs
```

`APIDocConfig` sets API metadata and JWT bearer auth scheme (`bearerAuth`) for Swagger/OpenAPI.

## Google OAuth2 Setup (Basic)

In Google Cloud Console:

1) Create OAuth Client ID (Web application)
2) Add authorized redirect URI:

```text
http://localhost:8083/login/oauth2/code/google
```

3) Put client ID/secret in `application-dev.properties` (or env vars)

Start login from browser:

```text
http://localhost:8083/oauth2/authorization/google
```

After successful Google login, backend creates/fetches user, stores refresh-token record, and sets refresh cookie.

## GitHub OAuth2 Setup (Basic)

In GitHub Developer Settings:

1) Create OAuth App
2) Add callback URL:

```text
http://localhost:8083/login/oauth2/code/github
```

3) Put client ID/secret in `application-dev.properties` (or env vars)

Start login from browser:

```text
http://localhost:8083/oauth2/authorization/github
```

After successful GitHub login, backend creates/fetches user and sets refresh cookie.

## Run Tests

```powershell
.\gradlew test
```

## API Overview

Auth base path: `/api/v1/auth`

- `POST /register` - Register user
- `POST /login` - Email/password login
- `POST /refresh` - Issue new access token using refresh token (body/cookie/header)
- `POST /logout` - Revoke refresh token, clear cookie, returns `204 No Content`

Users base path: `/api/v1/users` (secured)

- `POST /` - Create user
- `GET /` - List users
- `GET /email/{email}` - Get user by email
- `GET /{userId}` - Get user by id
- `PUT /{userId}` - Update user
- `DELETE /{userId}` - Delete user

OAuth2 entry endpoints:

- `GET /oauth2/authorization/google`
- `GET /oauth2/authorization/github`

## Security Notes

Public endpoints in current config:

- `/api/v1/auth/**`
- `/api/v1/public/**`
- `/v3/api-docs/**`
- `/swagger-ui/**`
- `/swagger-ui.html`

All other endpoints require authentication.

Use environment variables for secrets in real deployments.
