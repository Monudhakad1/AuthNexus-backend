# AuthNexus

AuthNexus is a Spring Boot backend focused on authentication and user management.

It currently includes:
- email/password registration and login
- JWT access + refresh token flow
- refresh token rotation with DB persistence
- user CRUD endpoints
- role-aware security foundation (`User` implements `UserDetails`)

This README is intentionally practical so a new developer can clone, run, and understand the code quickly.

## Tech Stack

- Java (toolchain in `build.gradle` is currently set to `Java 25`)
- Spring Boot (`4.0.2` in `build.gradle`)
- Spring Security
- Spring Data JPA
- MySQL
- Lombok
- MapStruct
- JJWT (`io.jsonwebtoken`)
- Gradle Wrapper

## Project Layout

```text
src/main/java/com/authnexus/centralapplication
├── config/                # Security configuration
├── controller/            # REST endpoints (`AuthController`, `UserController`)
├── domains/
│   ├── dto/               # API/request/response models
│   └── entities/          # JPA entities (`User`, `RefreshToken`, ...)
├── exception/             # Global exception handling
├── Helper/                # Small helper utilities
├── Mapper/                # DTO <-> Entity mapping
├── repository/            # Spring Data repositories
├── Security/              # JWT, cookie, and auth filter utilities
└── services/              # Business logic interfaces + implementations
```

## Prerequisites

- JDK matching your Gradle toolchain (current config: Java 25)
- MySQL running locally
- Git

## Quick Start

1) Clone and enter the repository
```powershell
git clone <your-repo-url>
cd AuthNexus
```

2) Create the database
```sql
CREATE DATABASE authnexus;
```

3) Review or update config in `src/main/resources/application-dev.properties`
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `security.jwt.secret`

4) Run the application
```powershell
.\gradlew.bat bootRun
```

By default, `application.properties` points to `dev` profile.
Current configured ports:
- `dev`: `8083`
- `qa`: `8081`
- `prod`: `8089`

## Running Tests

```powershell
.\gradlew.bat test
```

Current test coverage is minimal (`contextLoads` smoke test), so consider adding service and controller tests as you extend features.

## Configuration Notes

Main property files:
- `src/main/resources/application.properties`
- `src/main/resources/application-dev.properties`
- `src/main/resources/application-qa.properties`
- `src/main/resources/application-prod.properties`

JWT-related keys used by the app:
- `security.jwt.secret`
- `security.jwt.issuer`
- `security.jwt.access-ttl-seconds`
- `security.jwt.refresh-ttl-seconds`
- `security.jwt.refresh-token-cookie-name`
- `security.jwt.cookie-secure`
- `security.jwt.cookie-http-only`
- `security.jwt.cookie-same-site`
- `security.jwt.cookie-domain`

For team/project safety, prefer overriding secrets via environment variables instead of committing real secrets.

## API Overview (Current)

Base auth path: `/api/v1/auth`

- `POST /register` - Register a user
- `POST /login` - Login and receive access token; refresh token is handled with cookie support
- `POST /refresh` - Rotate refresh token and issue a new access token

Base user path: `/api/v1/users` (secured)

- `POST /` - Create user
- `GET /` - List users
- `GET /email/{email}` - Fetch user by email
- `GET /{userId}` - Fetch user by ID
- `PUT /{userId}` - Update user
- `DELETE /{userId}` - Delete user

Security behavior (from current config):
- Public: `POST /api/v1/auth/register`, `POST /api/v1/auth/login`
- All other routes require authentication

## Suggested Git Workflow

A lightweight workflow that works well for this repo:

- `main` -> stable branch
- `feature/<short-name>` -> feature branches
- small PRs with focused scope

Example:
```powershell
git checkout -b feature/add-refresh-token-tests
# make changes
.\gradlew.bat test
git add .
git commit -m "feat(auth): add refresh token service tests"
git push -u origin feature/add-refresh-token-tests
```

Commit style recommendation:
- `feat:` new functionality
- `fix:` bug fixes
- `refactor:` internal cleanup
- `test:` test-only changes
- `docs:` documentation

## Contribution Checklist

Before opening a PR:
- pull latest changes from `main`
- run tests locally
- avoid committing secrets
- keep commits readable and intentional
- include API impact notes if endpoints or contracts changed

## Current Gaps / TODOs Seen in Code

A few areas are marked or implied as in-progress:
- stronger validation for register/login payloads
- password update flow should enforce encoding
- broader automated tests (service/controller/security)
- role assignment hardening during registration

If you want, I can next generate:
1. a starter Postman collection for these endpoints
2. a `.env.example` + cleaned property strategy
3. a CI workflow (`.github/workflows/ci.yml`) for Gradle test on push/PR

