# AETHER — Engineering Handoff

Last updated: 2026-11-20

## 1. Current Status

Stage 0 — Backend Platform Foundation: COMPLETED.

Stage 1 — Identity, Access and Trust Foundation: IN PROGRESS.

### Completed implementation slices

#### Foundation — Slice 1: Backend Platform Foundation

Status: COMPLETED — locally validated.

- Java 21 and Spring Boot backend.
- Gradle build.
- PostgreSQL database.
- Flyway migration framework.
- Application and migration database roles.
- Health and readiness endpoints.
- Correlation IDs.
- Structured logging.
- Problem Details exception handling.
- OpenAPI documentation.
- Initial GitHub Actions CI.
- Six foundation tests passed.

#### AETHER-002A — User Registration and Email Verification

Status: COMPLETED — locally validated.

Implemented:

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
- Registration and verification audit events.
- Local verification-token email adapter.
- Production SMTP email adapter.
- Request validation and consistent API error responses.
- Integration tests for registration and verification.

Validation:

- Gradle test suite passed locally.
- Flyway V2 migration completed successfully.
- PostgreSQL migration history reports V1 and V2 as successful.
- Registration API tested successfully.
- Email verification API tested successfully.
- Invalid and reused token behavior tested.
- Local PostgreSQL account data verified.

The implementation has been tested locally. It has not yet been
released to production.

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
- springdoc-openapi 3.2.0
- Spring Security cryptography APIs
- Password4j
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

Identity tables introduced by V2:

- `user_account`
- `user_password_credential`
- `email_verification_token`
- `identity_audit_event`

The runtime application role does not own the schema and does not
receive general DDL privileges.

## 5. Identity Architecture

Identity implementation is divided into independent vertical slices.

### AETHER-002A — Registration and email verification

Completed locally.

Account lifecycle introduced:

`PENDING_VERIFICATION -> ACTIVE`

Additional account states:

- SUSPENDED
- CLOSED

Password storage:

- Argon2id.
- 19,456 KiB memory.
- 2 iterations.
- Parallelism 1.
- 32-byte output.

Verification token:

- 32 bytes generated using SecureRandom.
- URL-safe Base64 encoding without padding.
- SHA-256 hash stored in PostgreSQL.
- 24-hour expiry by default.
- Single-use consumption.
- Token consumption and account activation in one transaction.

Audit events:

- USER_REGISTRATION_REQUESTED
- USER_EMAIL_VERIFIED

Email delivery:

- Local profile: development-only log-based verification sender.
- Production profile: SMTP adapter.

The local verification-token logging adapter must never be used in
shared, staging or production environments.

### AETHER-002B — Authentication and session management

Status: NEXT.

Planned capabilities:

- Login using verified account credentials.
- Opaque access tokens.
- Refresh tokens.
- Hashed token storage.
- Refresh-token rotation.
- Replay detection.
- Session revocation.
- Logout.
- Authenticated current-user API.
- Spring Security request authentication.
- Authorization tests.
- User data isolation tests.

Authentication implementation must not allow unverified, suspended
or closed accounts to access protected personal APIs.

## 6. Security and Production Hardening Still Required

- Registration and login rate limiting.
- Abuse detection and protection.
- Email verification resend workflow.
- Transactional email outbox and retries.
- Password recovery.
- Session/device management.
- Operator identity separation and authorization.
- Security monitoring and alerting.
- Production email delivery validation.
- Production Argon2id work-factor benchmarking.
- Retention and account deletion policies.

Do not expose registration publicly until the required abuse controls
and delivery reliability mechanisms are implemented.

## 7. Local Development Environment

IDE: IntelliJ IDEA

Backend execution: Gradle `bootRun`

Database management: DBeaver

Database: PostgreSQL

IntelliJ configuration:

`AETHER API - Local`

Spring profile:

`local`

## 8. Current API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/v1/auth/register` | Register user |
| POST | `/api/v1/auth/verify-email` | Verify email |
| GET | `/actuator/health` | Application health |
| GET | `/actuator/health/liveness` | Liveness |
| GET | `/actuator/health/readiness` | Readiness |

## 9. Not Implemented Yet

- Login and authentication sessions.
- Refresh-token rotation.
- Logout and session revocation.
- Current-user endpoint.
- Device identity.
- Password recovery.
- Email verification resend.
- Android application.
- Admin Console.
- Conversation intelligence.
- Voice interaction.
- Personal memory.
- AI provider integration.
- Tasks and reminders.
- Notification intelligence.
- Proactive intelligence.
- Action execution.

## 10. Next Milestone

### AETHER-002B — Authentication and Session Management

The objective is to establish a secure authenticated user experience.

Planned implementation:

1. Login using verified credentials.
2. Opaque access-token issuance.
3. Refresh-token issuance and rotation.
4. Server-side session persistence.
5. Hashed refresh-token storage.
6. Refresh-token replay protection.
7. Logout and session revocation.
8. Spring Security bearer-token authentication.
9. Protected current-user endpoint.
10. Cross-user authorization tests.

The implementation must remain compatible with the planned
React Native Android application.

## 11. Engineering Workflow

- `main` remains production-ready.
- `develop` is the integration branch.
- Every implementation slice uses a dedicated feature branch.
- Changes are submitted through pull requests.
- Do not commit directly to `main` or `develop`.

For every slice:

1. Define the user problem and workflow.
2. Agree on acceptance criteria.
3. Design the required domain, API and database changes.
4. Implement complete functionality.
5. Validate automated tests and local integration.
6. Update architecture documentation and this handoff.
7. Commit and submit a pull request.
8. Merge only after validation passes.