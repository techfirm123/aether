# AETHER — Product and Engineering Roadmap

Version: 1.1
Last updated: 2026-11-20

## 1. Roadmap approach

AETHER will be developed through complete, validated vertical slices.

The roadmap describes capability progression, not fixed delivery dates.
A stage advances when its exit conditions are met.

Android is the initial mobile implementation and release priority.
iOS implementation follows a stable Android rollout.

The backend begins as a Java 21 / Spring Boot modular monolith.
The mobile application uses React Native and TypeScript.
The Platform Admin Console uses Next.js and TypeScript.

## 2. Current status

### Stage 0 — Product and platform foundation

Status: COMPLETED — locally validated.

Delivered:

- GitHub repository and branch foundation.
- Java 21 and Spring Boot 4.1.3 backend.
- Gradle 9.9.2.
- PostgreSQL datasource and database roles.
- Flyway migration framework.
- Initial AETHER schema migration.
- Health and readiness endpoints.
- Structured logging and correlation IDs.
- RFC 9457 Problem Details handling.
- OpenAPI documentation.
- Integration test foundation.
- Initial GitHub Actions CI workflow.
- Product principles and roadmap documentation.

Validation:

- Six foundation tests passed.
- PostgreSQL connectivity succeeded.
- Flyway V1 succeeded.
- Application started locally.
- Health, liveness and readiness returned UP.

Production deployment is not complete.

### Stage 1 — Identity, access and trust foundation

Status: IN PROGRESS.

Goal:

Establish secure user identity and access boundaries before introducing
personal user data and business APIs.

#### AETHER-002A — User registration and email verification

Status: COMPLETED — locally validated.

Delivered:

- User registration API.
- Email normalization.
- User account lifecycle foundation.
- Argon2id password hashing.
- Separate password credential storage.
- Cryptographically secure verification token generation.
- SHA-256 verification-token hash storage.
- 24-hour token expiry.
- Single-use token enforcement.
- Email verification API.
- Account activation.
- Identity audit events.
- Local verification-token sender.
- Production SMTP sender.
- Registration and verification integration tests.

Validation:

- Gradle test suite passed locally.
- PostgreSQL Flyway V2 succeeded.
- Registration API worked.
- Email verification API worked.
- Invalid and reused tokens were rejected.

#### AETHER-002B — Authentication and session management

Status: NEXT.

Planned functionality:

- Login using verified account credentials.
- Opaque access tokens.
- Refresh tokens.
- Server-side session persistence.
- Hashed refresh-token storage.
- Token rotation and replay protection.
- Session revocation.
- Logout.
- Spring Security bearer-token authentication.
- Authenticated current-user endpoint.
- User ownership and cross-user isolation tests.

#### Subsequent identity slices

- Account recovery and password reset.
- Email verification resend.
- Registration and login rate limiting.
- Abuse protection.
- Device enrollment and trusted device management.
- Separate platform-operator identity.
- Operator authorization and stronger authentication.

Platform operators must not receive unrestricted access to personal
user content.

Stage exit conditions:

- Users can create accounts and authenticate securely.
- Account and session lifecycle is defined and tested.
- Protected APIs enforce authentication.
- User ownership and authorization boundaries are testable.
- Security-sensitive operations generate audit events.
- PostgreSQL migrations are validated.
- Critical account recovery and security requirements are addressed.

### Stage 2 — Android application foundation

Goal:

Establish the first usable Android application using the shared
React Native + TypeScript codebase.

Planned capabilities:

- Initialize the mobile application.
- Establish navigation and application structure.
- Environment and API configuration.
- Onboarding and authentication screens.
- Connect to the AETHER API.
- Secure local credential and session handling.
- Android-specific Kotlin integrations where required.
- Crash diagnostics without collecting personal content.

Stage exit conditions:

- Android application builds reliably.
- User can register, sign in and access the authenticated experience.
- API errors and expired sessions are handled correctly.
- Basic navigation and lifecycle behavior are validated.

### Stage 3 — Conversational and voice intelligence

Goal:

Build AETHER's first meaningful companion interaction.

Planned capabilities:

- Conversation lifecycle.
- Push-to-talk interaction.
- Audio capture and playback.
- Speech recognition and response generation.
- Synchronized transcript and conversation state.
- AI orchestration boundary.
- Provider-independent model integration.
- Configurable local/cloud provider selection.
- Timeouts, retries and graceful failure behavior.
- Conversation privacy and retention controls.

The first implementation should focus on one validated provider path.

Stage exit conditions:

- User can complete a useful voice interaction.
- Visual interface reflects the interaction state.
- AI failures are handled safely and clearly.
- Provider integration can evolve independently of core workflows.

### Stage 4 — Personal memory and context

Goal:

Give AETHER useful continuity across interactions.

Planned capabilities:

- Personal memory model.
- Explicit memory creation and updates.
- Memory relevance and retrieval.
- Context assembly for conversations.
- Memory inspection and correction.
- Memory deletion and retention controls.
- Source and provenance tracking where appropriate.
- User-level privacy boundaries.

PostgreSQL remains the primary database. pgvector will be introduced
when concrete semantic retrieval requirements justify it.

Stage exit conditions:

- Relevant memories improve subsequent interactions.
- Users can inspect, correct and delete retained memories.
- Retrieval is isolated to the authenticated user's data.
- Memory behavior is tested for relevance, privacy and deletion.

### Stage 5 — Personal capabilities

Goal:

Allow AETHER to help with practical everyday workflows.

Candidate capabilities:

- Tasks and to-dos.
- Reminders.
- Calendar and schedule understanding.
- Notifications and notification intelligence.
- Selected external integrations.
- Personal information organization.

Prioritize capabilities by actual user value. Do not implement all
candidate capabilities as one large release.

Stage exit conditions:

- Selected workflows work end to end.
- User data remains correctly isolated.
- Important changes are visible and controllable.
- Failures do not create duplicate or misleading outcomes.

### Stage 6 — Proactive intelligence and action execution

Goal:

Move from reactive assistance toward context-aware, proactive help.

Planned capabilities:

- Relevant event and condition detection.
- User-configurable proactive preferences.
- Suggestions and recommendations.
- Action planning.
- Approval and rejection workflows.
- Action execution boundaries.
- Execution history and audit records.
- Cancellation and recovery where supported.
- Idempotency and duplicate-action protection.

Important actions remain approval-based.

Stage exit conditions:

- Proactive suggestions are useful and configurable.
- Proposed actions are understandable before approval.
- Execution results are verified and accurately reported.
- Rejected or unapproved actions are not executed.

### Stage 7 — Platform operations and Admin Console

Goal:

Provide platform operators with information needed to operate and
improve AETHER.

Planned capabilities:

- Separate operator authentication.
- Role-based operator authorization.
- Aggregate application adoption.
- Aggregate registrations and engagement.
- Feature usage.
- AI provider consumption and cost.
- API reliability and error rates.
- Deployment and service health.
- Operational reporting.

The Admin Console is a separate Next.js + TypeScript application.

It must not expose routine access to personal conversations, memories,
notifications, documents or other private user content.

Stage exit conditions:

- Operators can understand adoption and platform health.
- Access is restricted by operator role.
- Analytics are designed around aggregate operational needs.
- Private content is excluded from routine admin views and telemetry.

### Stage 8 — Android production readiness and rollout

Goal:

Prepare AETHER for a controlled production release.

Planned capabilities and controls:

- Security and privacy review.
- Authentication and authorization review.
- Performance and battery optimization.
- Reliability and resilience testing.
- Crash reporting and operational monitoring.
- Database backup and restore validation.
- Deployment and rollback procedures.
- Production secrets management.
- Staging and smoke testing.
- Release approval and staged rollout.
- Support and incident procedures.

Stage exit conditions:

- Production readiness checks pass.
- Critical workflows are tested on supported Android devices.
- Monitoring and recovery procedures are established.
- A controlled Android rollout is successful.

### Stage 9 — iOS implementation

Goal:

Extend the shared mobile application to iOS after Android is stable
and rolled out.

Planned capabilities:

- iOS build and release configuration.
- Swift-specific native integrations.
- iOS permissions and background execution behavior.
- Voice and audio lifecycle adaptation.
- Platform-specific testing and performance work.

Stage exit conditions:

- Core workflows work consistently on iOS.
- iOS-specific privacy and permission behavior is validated.
- Experience meets the same product quality expectations.

## 3. Cross-cutting engineering quality

Each implementation slice must include appropriate:

- Unit and integration tests.
- API contract validation.
- PostgreSQL migration validation.
- Authentication and authorization tests where relevant.
- Privacy and data exposure checks.
- Logging and diagnostics that avoid personal content.
- Failure handling and recovery behavior.
- Architecture and handoff documentation.

CI/CD will evolve incrementally.

Current baseline:

- GitHub Actions backend build and test workflow.

Planned CI growth:

1. Backend build and test validation.
2. PostgreSQL migration verification.
3. Code quality and static analysis.
4. Dependency and security checks.
5. Mobile lint, type checks, tests and Android build.
6. Admin lint, type checks, tests and production build.
7. End-to-end and regression tests.
8. Staging deployment and smoke tests.
9. Production approval and post-deployment verification.

Required GitHub checks must report reliably for every relevant pull
request. Component path filters must not leave required checks pending.

## 4. Delivery workflow

- `main` remains production-ready.
- `develop` is the integration branch.
- Each feature or bug fix uses its own branch from `develop`.
- Urgent production fixes use a hotfix branch from `main`.
- Changes are submitted through pull requests.
- Do not commit directly to `main` or `develop`.

For every vertical slice:

1. Define the user problem and workflow.
2. Confirm scope and acceptance criteria.
3. Design required UX, domain, API and data changes.
4. Implement complete functionality.
5. Validate locally and through automated tests.
6. Update architecture and AETHER_HANDOFF.md.
7. Commit and submit a pull request.
8. Merge only after required validation passes.

## 5. Scope discipline

The roadmap is an evolving plan, not a commitment to implement every
candidate capability.

Prioritize:

1. User value.
2. Privacy and trust.
3. End-to-end completeness.
4. Reliability.
5. Maintainability.

Do not build speculative modules, integrations, abstractions or
infrastructure before a real vertical slice requires them.