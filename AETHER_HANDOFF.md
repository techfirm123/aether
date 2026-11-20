# AETHER — Engineering Handoff

Last updated: 2026-11-20

## 1. Current Status

Stage 0 — Backend Platform Foundation: COMPLETED.

Stage 1 — Identity, Access and Trust Foundation: CORE IMPLEMENTATION COMPLETED.

### Completed implementation slices

#### Foundation — Slice 1: Backend Platform Foundation

Status: COMPLETED — locally validated.

Delivered:

- Java 21 and Spring Boot backend.
- Gradle build configuration.
- PostgreSQL database.
- Flyway migration framework.
- Separate application and migration database roles.
- Health and readiness endpoints.
- Request correlation IDs.
- Structured logging.
- RFC 9457 Problem Details exception handling.
- OpenAPI documentation.
- Initial GitHub Actions CI workflow.

Validation:

- 6/6 foundation tests passed.
- PostgreSQL connection succeeded.
- Flyway V1 succeeded.
- Application started locally.
- Health, liveness and readiness returned UP.

#### AETHER-002A — User Registration and Email Verification

Status: COMPLETED — locally validated and merged into develop.

Delivered:

- User registration API.
- Email normalization.
- User account lifecycle foundation.
- Argon2id password hashing.
- Separate password credential storage.
- Cryptographically secure email verification tokens.
- SHA-256 token hash storage.
- 24-hour verification-token expiry.
- Single-use verification tokens.
- Email verification API.
- Account activation after successful verification.
- Identity audit events.
- Local verification-token sender.
- Production SMTP sender.
- Request validation and consistent API error responses.

Validation:

- Gradle test suite passed locally.
- Flyway V2 migration succeeded on PostgreSQL.
- Registration API tested successfully.
- Email verification API tested successfully.
- Invalid and reused token behavior tested.
- Local PostgreSQL account data verified.

#### AETHER-002B — Authentication and Session Management

Status: IMPLEMENTATION COMPLETED — locally validated.

Delivered:

- Login using verified account credentials.
- Generic authentication failure response.
- Opaque access-token issuance.
- Opaque refresh-token issuance.
- SHA-256 token hash storage.
- Server-managed PostgreSQL sessions.
- Ten-minute access-token lifetime.
- Thirty-day absolute session/refresh-token lifetime.
- Refresh-token rotation.
- Refresh-token reuse detection.
- Session revocation.
- Logout endpoint.
- Spring Security bearer-token authentication.
- Protected current-user endpoint.
- Security audit events.
- Authentication entry point using RFC 9457 Problem Details.
- Correlation IDs on security failures.

Validation:

- Gradle test suite passed locally.
- Flyway V3 migration succeeded on PostgreSQL.
- Application started successfully through IntelliJ Gradle configuration.
- Login tested successfully against PostgreSQL.
- Authenticated current-user API tested successfully.
- Refresh-token rotation tested successfully.
- Previous access token was rejected after refresh.
- Newly issued access token was accepted.
- PostgreSQL session and refresh-token records verified.
- Security failure response included a correlation ID.

Automated integration tests cover:

- Unauthenticated access rejection.
- Unverified account login rejection.
- Verified user login.
- Incorrect password rejection.
- Access and refresh token persistence.
- Refresh-token rotation.
- Refresh-token replay detection and session revocation.
- Logout and session revocation.
- User identity isolation.
- Protected unknown API route handling.

Manual PostgreSQL testing of refresh-token replay and logout can be
repeated through the mobile application workflow. These are not
represented as manually completed production validations.

The implementation has been tested locally. It has not been released
to production.

## 2. Product Vision

AETHER is a personal intelligence companion designed to understand an
individual's world, remember what matters, anticipate needs, and help
execute approved actions.

Core principles:

- Personal and context-aware.
- Voice-first, not voice-only.
- User-controlled persistent memory.
- Privacy before convenience.
- Trust before autonomy.
- Proactive but considerate.
- Explainable and honest.
- Provider-independent AI.
- Operational visibility without routine access to private content.
- Reliable before sophisticated.

Refer to:

- `docs/PRODUCT_PRINCIPLES.md`
- `docs/ROADMAP.md`

## 3. Technology Baseline

- Java 21
- Spring Boot 4.1.3
- Gradle 9.9.2
- PostgreSQL
- Flyway
- Spring MVC
- Spring Boot Actuator
- Spring Security
- Spring Security cryptography APIs
- Password4j
- springdoc-openapi 3.2.0
- React Native + TypeScript (planned)
- Kotlin for Android native integrations (planned)
- Next.js + TypeScript for Admin Console (planned)

## 4. Database Configuration

Database: `aether`

Schema: `aether`

Migration role: `aether_migrator`

Runtime role: `aether_app`

Successful migrations:

| Version | Migration | Status |
|---|---|---|
| V1 | create aether schema | Successful |
| V2 | create user identity foundation | Successful |
| V3 | create user sessions | Successful |

Identity tables:

- `user_account`
- `user_password_credential`
- `email_verification_token`
- `identity_audit_event`
- `user_session`
- `user_refresh_token`

The runtime application role does not own the schema and does not
receive general DDL privileges.

## 5. Identity Architecture

End-user identity and future platform-operator identity are separate
security contexts.

### Account lifecycle

Initial states:

- PENDING_VERIFICATION
- ACTIVE
- SUSPENDED
- CLOSED

Implemented transition:

`PENDING_VERIFICATION -> ACTIVE`

Successful email verification activates the account.

Login is permitted only for ACTIVE accounts.

### Password storage

Passwords are never stored in plaintext or reversibly encrypted.

Password hashing uses Argon2id through Spring Security's PasswordEncoder
abstraction and Password4j.

Initial parameters:

- Memory: 19,456 KiB.
- Iterations: 2.
- Parallelism: 1.
- Output length: 32 bytes.

The work factor must be benchmarked on target hardware and tuned before
production deployment.

### Email verification

- 32 random bytes generated using SecureRandom.
- URL-safe Base64 encoding without padding.
- SHA-256 token hash stored in PostgreSQL.
- Default validity: 24 hours.
- Single-use token consumption.
- Token consumption and account activation in one transaction.

The local profile uses a development-only sender that logs the raw
verification token. The production profile uses SMTP.

The local token-logging adapter must never be enabled in shared,
staging or production environments.

### Authentication tokens

Access token:

- Opaque random credential.
- 256 bits of entropy.
- `aet_` prefix.
- Ten-minute lifetime.
- SHA-256 hash stored in the session record.

Refresh token:

- Opaque random credential.
- 256 bits of entropy.
- `aer_` prefix.
- Thirty-day absolute session lifetime.
- SHA-256 hash stored in the refresh-token table.
- Rotated on successful refresh.

Raw access and refresh credentials are returned only to the client.
They are not stored in PostgreSQL.

### Session behavior

- Sessions are persisted in PostgreSQL.
- The latest access-token hash is associated with the session.
- Successful refresh replaces the access token and refresh token.
- Previous access tokens become invalid after rotation.
- Refresh tokens are single-use.
- Reuse of a consumed refresh token revokes the associated session.
- Logout revokes the current session.
- Revoked sessions cannot authenticate API requests.

Session and refresh-token history are retained in the database for
security auditability.

## 6. Authentication APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/v1/auth/register` | Register user |
| POST | `/api/v1/auth/verify-email` | Verify email |
| POST | `/api/v1/auth/login` | Authenticate user |
| POST | `/api/v1/auth/refresh` | Rotate session tokens |
| POST | `/api/v1/auth/logout` | Revoke current session |
| GET | `/api/v1/users/me` | Retrieve authenticated user |
| GET | `/actuator/health` | Application health |
| GET | `/actuator/health/liveness` | Liveness |
| GET | `/actuator/health/readiness` | Readiness |

Protected APIs use the HTTP Authorization bearer-token header.

Unauthenticated access returns HTTP 401 with an RFC 9457 Problem Details
response.

## 7. Security Audit Events

Implemented event types include:

- USER_REGISTRATION_REQUESTED
- USER_EMAIL_VERIFIED
- LOGIN_SUCCEEDED
- LOGIN_REJECTED
- SESSION_REFRESHED
- SESSION_REVOKED
- REFRESH_TOKEN_REUSE_DETECTED

Audit events contain identity references, event types, correlation IDs
and occurrence timestamps.

They must never contain passwords, raw access tokens, raw refresh
tokens, raw verification tokens or personal message content.

## 8. Security and Production Hardening Still Required

- Registration and login rate limiting.
- Abuse detection and protection.
- Email verification resend workflow.
- Password recovery.
- Transactional email outbox and retries.
- Device identity and trusted-device management.
- Stronger operator authentication and authorization.
- Production Argon2id work-factor benchmarking.
- Session/device management UI.
- Security monitoring and alerting.
- Retention and account deletion policies.
- Mobile secure credential storage.
- Mobile refresh serialization and retry handling.
- Production deployment and security review.

Do not expose registration publicly until the necessary abuse controls
and delivery reliability mechanisms are implemented.

## 9. Local Development Environment

IDE: IntelliJ IDEA

Backend execution: Gradle `bootRun`

Database management: DBeaver

Database: PostgreSQL

IntelliJ configuration:

`AETHER API - Local`

Spring profile:

`local`

## 10. Next Milestone

Stage 2 — Android Application Foundation.

Objective:

Build the first usable AETHER mobile application using a shared
React Native + TypeScript codebase, with Android as the initial
implementation and release priority.

Initial mobile slices:

1. React Native application initialization.
2. Navigation and application structure.
3. Environment and API configuration.
4. Login and registration experience.
5. Authentication API integration.
6. Secure local token storage.
7. Refresh-token handling.
8. Logout.
9. Authenticated application shell.
10. Loading, error and expired-session states.

The mobile UI will become the primary end-to-end validation surface
for registration, verification, login, refresh, replay and logout.

## 11. Engineering Workflow

- `main` remains production-ready.
- `develop` is the integration branch.
- Every implementation slice uses a dedicated feature branch.
- Changes are submitted through pull requests.
- Do not commit directly to `main` or `develop`.

For every slice:

1. Define the user problem and expected workflow.
2. Agree on acceptance criteria.
3. Design required UX, domain, API and database changes.
4. Implement complete functionality.
5. Validate automated tests and local integration.
6. Update architecture documentation and this handoff.
7. Commit and submit a pull request.
8. Merge only after required validation passes.