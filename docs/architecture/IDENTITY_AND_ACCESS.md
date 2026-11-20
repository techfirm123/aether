# AETHER — Identity and Access Architecture

Last updated: 2026-11-20

## 1. Scope

Identity and access are foundational platform capabilities.

Implementation is divided into vertical slices:

- AETHER-002A: user registration and email verification.
- AETHER-002B: authentication and session management.
- Future slices: account recovery, device identity and
  platform-operator access.

## 2. Identity boundaries

End-user identity and platform-operator identity are separate security
contexts.

Platform operators must not receive unrestricted personal-data access
through ordinary user roles.

The current implementation supports end-user registration and
authentication.

## 3. Account lifecycle

Initial states:

- PENDING_VERIFICATION
- ACTIVE
- SUSPENDED
- CLOSED

Implemented transition:

PENDING_VERIFICATION -> ACTIVE

An account becomes ACTIVE only after successful email verification.

Login is allowed only for ACTIVE accounts.

Pending, suspended and closed accounts cannot authenticate.

## 4. Password storage

Passwords are never stored in plaintext or reversibly encrypted.

Password hashing uses Spring Security's PasswordEncoder abstraction
with Password4j-backed Argon2id.

Initial parameters:

- Memory: 19,456 KiB.
- Iterations: 2.
- Parallelism: 1.
- Output length: 32 bytes.

The work factor must be benchmarked on target hardware and tuned before
production deployment.

## 5. Email verification

Implemented behavior:

- Generate 32 random bytes using SecureRandom.
- Encode raw token using URL-safe Base64 without padding.
- Store only the SHA-256 token hash.
- Default validity: 24 hours.
- Single-use token consumption.
- Account activation and token consumption in one transaction.
- Invalid, expired and consumed tokens receive the same public error.

The local profile uses a development-only sender that logs the raw
verification token.

The production profile uses the SMTP adapter.

Never enable the local token-logging sender in shared, staging or
production environments.

## 6. Registration privacy

The registration API returns the same accepted response for existing
and newly submitted addresses.

Password hashing is performed before the existence check to reduce
timing differences.

This does not replace rate limiting or abuse protection.

These controls remain required before public exposure.

## 7. Persistence

Flyway V2 creates:

- user_account
- user_password_credential
- email_verification_token
- identity_audit_event

Flyway V3 creates:

- user_session
- user_refresh_token

All tables are in the aether schema.

The runtime role has the privileges required for the implemented
account, credential, token and session workflows.

Account and session records are revoked or closed rather than
physically deleted through normal runtime operations.

Audit events are insert-only for the runtime application role.

Audit records must never contain passwords, raw verification tokens,
raw access tokens, raw refresh tokens or personal message content.

## 8. Email delivery boundary

VerificationEmailSender is an application abstraction.

Implemented adapters:

- LoggingVerificationEmailSender for local development.
- SmtpVerificationEmailSender for the production profile.

The current registration implementation sends synchronously.

A transactional outbox, retry and resend workflow is required before
public-scale operation.

## 9. AETHER-002A — Registration and verification

Status: COMPLETED — locally validated.

Delivered:

- Registration endpoint.
- Email normalization.
- Password hashing.
- Verification-token generation and hashing.
- Verification-token expiry.
- Token single-use enforcement.
- Account activation.
- Security audit events.
- Local and production email sender adapters.
- Integration test coverage.

Validation:

- Local Gradle test suite passed.
- PostgreSQL V2 migration succeeded.
- Registration API succeeded.
- Email verification API succeeded.
- Invalid and reused tokens were rejected.

## 10. AETHER-002B — Authentication and sessions

Status: IMPLEMENTATION COMPLETED — locally validated.

### Access tokens

- Opaque random credentials.
- 256 bits of entropy.
- `aet_` prefix.
- Ten-minute lifetime.
- SHA-256 hash stored in user_session.
- Never stored or logged in raw form.

### Refresh tokens

- Opaque random credentials.
- 256 bits of entropy.
- `aer_` prefix.
- Thirty-day absolute session lifetime.
- SHA-256 hash stored in user_refresh_token.
- Single-use rotation.

### Session lifecycle

A successful login creates:

- A user_session record.
- An access-token hash.
- An initial refresh-token record.
- A LOGIN_SUCCEEDED audit event.

A successful refresh:

1. Locks the refresh-token and session rows.
2. Verifies that the refresh token is unconsumed and unexpired.
3. Checks that the session is active and the account is ACTIVE.
4. Creates replacement access and refresh credentials.
5. Stores the replacement refresh-token hash.
6. Marks the previous refresh token as consumed.
7. Links the previous token to the replacement token.
8. Replaces the session's access-token hash.
9. Records a SESSION_REFRESHED audit event.

The previous access token becomes invalid after rotation.

### Refresh-token replay

A previously consumed refresh token cannot be reused.

When a consumed token is presented again:

- The associated active session is revoked.
- The revocation reason is REFRESH_TOKEN_REUSE.
- A REFRESH_TOKEN_REUSE_DETECTED audit event is recorded.
- The refresh operation returns an authentication failure.

The replay revocation must commit before the API returns the failure.

### Logout

Logout revokes the authenticated session.

The session's access token becomes invalid immediately for subsequent
authentication requests.

A SESSION_REVOKED audit event is recorded.

### Authentication

Spring Security authenticates requests using bearer access tokens.

Protected APIs resolve the authenticated user and session from the
validated access-token hash.

The API does not trust a user ID supplied by the client to establish
identity.

The current-user endpoint returns identity belonging to the
authenticated principal.

### Validation

- Automated authentication integration tests passed.
- PostgreSQL V3 migration succeeded.
- Application startup succeeded.
- Login was manually tested against PostgreSQL.
- Authenticated current-user API was manually tested.
- Refresh rotation was manually tested.
- Previous access-token invalidation was manually tested.
- New access-token acceptance was manually tested.
- PostgreSQL session and refresh-token records were inspected.
- Security failure responses include correlation IDs.

Manual UI testing of logout and refresh-token replay is deferred to
the Android application workflow. Automated integration tests cover
both behaviors.

## 11. API endpoints

| Method | Endpoint | Access |
|---|---|---|
| POST | /api/v1/auth/register | Public |
| POST | /api/v1/auth/verify-email | Public |
| POST | /api/v1/auth/login | Public |
| POST | /api/v1/auth/refresh | Public |
| POST | /api/v1/auth/logout | Authenticated |
| GET | /api/v1/users/me | Authenticated |

## 12. Explicitly deferred

- Social login.
- Multi-factor authentication.
- Trusted device enrollment.
- Password recovery.
- Email verification resend.
- Public registration and login abuse controls.
- Token/session management UI.
- Platform operator login.
- Admin Console authorization.
- Production deployment security review.

These capabilities should be introduced through separate slices when
their workflows and requirements are defined.