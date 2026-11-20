# AETHER — Identity and Access Architecture

Last updated: 2026-11-20

## 1. Scope

Identity and access are foundational platform capabilities.

Implementation is divided into vertical slices:

- AETHER-002A: registration and email verification.
- AETHER-002B: authentication and session management.
- Future slices: device identity, recovery and platform-operator access.

## 2. Identity boundaries

End-user identity and platform-operator identity are separate security
contexts.

Platform operators must not receive unrestricted personal-data access
through ordinary user roles.

The current implementation supports end-user registration only.

## 3. Account lifecycle

Initial states:

- PENDING_VERIFICATION
- ACTIVE
- SUSPENDED
- CLOSED

The implemented initial transition is:

PENDING_VERIFICATION -> ACTIVE

An account becomes ACTIVE only after successful email verification.

## 4. Password storage

Passwords are never stored as plaintext or reversibly encrypted.

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

All tables are in the aether schema.

The runtime role has the privileges required for the implemented
account and token workflows. It cannot physically delete account,
credential or verification-token records.

Audit events are insert-only for the runtime application role.

Audit records must never contain passwords, raw verification tokens
or personal message content.

## 8. Email delivery boundary

VerificationEmailSender is an application abstraction.

Implemented adapters:

- LoggingVerificationEmailSender for local development.
- SmtpVerificationEmailSender for the production profile.

The current registration implementation sends synchronously.
A transactional outbox, retry and resend workflow is required before
public-scale operation.

## 9. AETHER-002A implementation status

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

## 10. AETHER-002B implementation scope

Next:

- Verified-account login.
- Opaque access credentials.
- Refresh credentials.
- Server-side session records.
- Hashed refresh-token storage.
- Rotation and replay detection.
- Session revocation.
- Logout.
- Spring Security bearer authentication.
- Current-user endpoint.
- Cross-user isolation tests.

Authentication must reject pending, suspended and closed accounts.

## 11. Explicitly deferred

- Social login.
- Multi-factor authentication.
- Trusted device enrollment.
- Password recovery.
- Email verification resend.
- Public registration abuse controls.
- Platform operator login.
- Admin Console authorization.

These capabilities should be introduced through separate slices when
their workflows and requirements are defined.