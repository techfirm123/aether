# AETHER — Engineering Handoff

Last updated: 2026-11-20

## 1. Current Status

Foundation — Slice 1: Backend Platform Foundation

Status: COMPLETED — Local validation successful.

Product principles and roadmap have been formalized in:

- `docs/PRODUCT_PRINCIPLES.md`
- `docs/ROADMAP.md`

The roadmap identifies Identity and Access Management as the next
implementation stage.

## 2. Product Vision

AETHER is a personal intelligence companion designed to understand an
individual's world, remember what matters, anticipate needs, and help
execute approved actions.

Core product direction:

- Voice-first personal AI companion with synchronized visual experience.
- Persistent personal memory and contextual understanding.
- Proactive, considerate assistance.
- Approval-based execution for important actions.
- Privacy-conscious local/cloud AI processing.
- Provider-independent AI architecture.
- Android-first mobile implementation.
- Separate Platform Admin Console.
- Operational visibility without routine access to private content.

Refer to `docs/PRODUCT_PRINCIPLES.md` for the complete principles.

Refer to `docs/ROADMAP.md` for the staged capability roadmap.

## 3. Product and Architecture Decisions

### Mobile

- Shared React Native + TypeScript application.
- Android is the first implementation and release priority.
- iOS development begins after Android is stable and rolled out.
- Kotlin handles Android-specific native integrations.
- Swift is reserved for future iOS-specific integrations.

### Backend

- Java 21.
- Spring Boot 4.1.3.
- Gradle 9.9.2.
- Modular monolith initially.
- PostgreSQL as the primary database.
- Flyway for schema migrations.

### AI

- Provider-independent architecture.
- Local and cloud model support.
- Explicit privacy-aware data routing.
- Approval-based action execution.
- Persistent memory with user controls.

### Admin Console

- Separate Next.js + TypeScript application.
- Platform operator identity and authorization.
- Aggregate adoption, feature usage, AI consumption,
  reliability and operational reporting.
- No routine access to private user conversations, memories,
  notifications or documents.

### Engineering Method

- Small, complete vertical slices.
- Feature branches from `develop`.
- Pull requests for integration.
- `main` remains production-ready.
- Complete source files and architecture documentation.
- Validate every slice before merging.

## 4. Technology Baseline

- Java 21
- Spring Boot 4.1.3
- Gradle 9.9.2
- PostgreSQL
- Flyway
- Spring MVC
- Spring Boot Actuator
- springdoc-openapi 3.2.0
- React Native + TypeScript (planned)
- Kotlin for Android integrations (planned)
- Next.js + TypeScript for Admin Console (planned)
- Encrypted SQLite for mobile local storage (planned)
- pgvector when memory retrieval requirements justify it (planned)

## 5. Foundation Implementation

Completed:

- Backend project structure.
- Gradle build configuration.
- Environment-specific configuration.
- PostgreSQL datasource configuration.
- Separate application and migration database roles.
- Flyway migration framework.
- Initial AETHER schema migration.
- Liveness and readiness endpoints.
- Structured logging.
- Request correlation IDs.
- RFC 9457 Problem Details exception handling.
- OpenAPI documentation.
- Integration test foundation.
- GitHub Actions backend CI workflow.
- Initial architecture documentation.
- Mobile and Admin Console project placeholders.

## 6. Database Configuration

Database: `aether`

Schema: `aether`

Migration role: `aether_migrator`

Application role: `aether_app`

Migration history:

`aether.flyway_schema_history`

Successful migration:

`V1__create_aether_schema.sql`

Migration version: 1

Description: create aether schema

Installed by: `aether_migrator`

Success: true

## 7. Validation Results

### Automated Tests

6/6 tests passed.

1. Liveness endpoint.
2. Readiness endpoint.
3. OpenAPI document availability.
4. Generated correlation ID.
5. Preservation of valid caller correlation ID.
6. Unknown route handling.

### PostgreSQL

- Database created successfully.
- Migration role created.
- Application role created.
- Database ownership verified.
- Schema ownership verified.
- Application and migration accounts configured.
- Flyway V1 successfully executed.

### Runtime

- Spring Boot started through IntelliJ Gradle configuration.
- Application health returned UP.
- Liveness returned UP.
- Readiness returned UP.
- Swagger UI loaded successfully.

Swagger currently contains no application API operations.
This is expected because application business controllers have not
yet been introduced.

### CI

The initial GitHub Actions backend build-and-test workflow has
been established.

CI/CD will expand incrementally as backend, mobile and admin
capabilities are introduced.

## 8. Local Development Environment

IDE: IntelliJ IDEA

Backend execution: Gradle `bootRun`

Database management: DBeaver

Database: PostgreSQL

IntelliJ configuration:

`AETHER API - Local`

Spring profile:

`local`

## 9. Not Implemented Yet

- User identity and authentication.
- Account verification and recovery.
- Session and device management.
- Platform operator identity and authorization.
- Android mobile application.
- Admin Console.
- Conversation intelligence.
- Voice interaction.
- Personal memory and contextual retrieval.
- AI provider integration.
- Tasks and reminders.
- Notification intelligence.
- Proactive intelligence.
- Action execution.

## 10. Next Milestone

Stage 1 — Identity, Access and Trust Foundation

Next implementation slice:

**Slice 2 — User Identity and Authentication**

Scope to define during slice discovery and design:

1. User registration and account lifecycle.
2. Secure credential storage.
3. Authentication.
4. Session management and logout.
5. Account verification and recovery requirements.
6. Current-user identity resolution.
7. Authorization foundation.
8. User data ownership boundaries.
9. Security audit events.
10. Authentication and authorization tests.

Important architectural boundaries:

- End-user identity is separate from platform operator identity.
- Platform operators must not receive unrestricted personal-data access.
- Device enrollment is a distinct capability and should be introduced
  when the mobile application requires it.
- Authentication/session technology must be selected through an
  explicit security design rather than assumed in advance.

## 11. Engineering Workflow

For every implementation slice:

1. Define the user problem and expected workflow.
2. Agree on scope and acceptance criteria.
3. Design the required UX, domain, API and database changes.
4. Implement the complete functionality.
5. Validate locally and through automated tests.
6. Update architecture documentation and this handoff.
7. Commit changes on the feature branch.
8. Submit a pull request.
9. Merge only after required validation passes.

The `main` branch must remain production-ready.