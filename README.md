# Project AETHER

AETHER is a personal intelligence companion designed to understand an individual's world, remember what matters, anticipate needs, and help execute approved actions.

## Product direction

- Voice-first personal AI companion with a synchronized visual experience.
- Memory-driven, context-aware, proactive and action-oriented.
- Privacy-conscious hybrid local/cloud processing.
- Approval-based execution for important actions.
- Android-first product rollout, with iOS planned after the Android application is stable and released.
- A separate Platform Admin Console for product operations and aggregated analytics.

## Technology direction

| Area | Technology |
|---|---|
| Shared mobile application | React Native + TypeScript |
| Android-specific integrations | Kotlin |
| Future iOS-specific integrations | Swift |
| Backend | Java 21 + Spring Boot 4.1.3 |
| Backend build | Gradle 9.9.2 |
| Database | PostgreSQL |
| Database migrations | Flyway |
| Vector search (future memory slice) | pgvector |
| Admin Console | Next.js + TypeScript |
| Mobile local storage | Encrypted SQLite (future mobile slice) |

## Repository layout

```text
aether/
├── apps/
│   ├── mobile/        # React Native app; Android-first implementation
│   └── admin/         # Next.js Platform Admin Console
├── services/
│   └── api/           # Java 21 / Spring Boot modular monolith
├── docs/
│   └── architecture/
├── .github/workflows/
├── AETHER_HANDOFF.md
└── README.md
```

The mobile and admin application directories are intentionally placeholders in the initial backend foundation slice. They will be initialized when their implementation slices begin.

## Backend: local prerequisites

- JDK 21
- Gradle 9.9.2 (used once to generate the Gradle Wrapper if the wrapper files are not yet present)
- PostgreSQL 15 or later (PostgreSQL 18 is also suitable)

Create a local database and application role using an administrative PostgreSQL account:

```sql
CREATE ROLE aether_migrator WITH LOGIN PASSWORD 'replace-with-a-local-migration-password';
CREATE ROLE aether_app WITH LOGIN PASSWORD 'replace-with-a-local-application-password';
CREATE DATABASE aether OWNER aether_migrator;
REVOKE CONNECT ON DATABASE aether FROM PUBLIC;
GRANT CONNECT ON DATABASE aether TO aether_migrator;
GRANT CONNECT ON DATABASE aether TO aether_app;
```

The password above is a local example only. Do not commit real credentials.

From `services/api`, generate the Gradle Wrapper once:

```bash
gradle wrapper --gradle-version 9.9.2 --gradle-distribution-sha256-sum 971fcd7150ca4ba04852c0b2ca0d2d0ab9468e14b03473eab19528809914c76b
```

Commit the generated `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.jar` files. The wrapper properties file is already provided.

Configure local database credentials in the shell or IDE run configuration:

```bash
export AETHER_DB_URL='jdbc:postgresql://localhost:5432/aether'
export AETHER_DB_PASSWORD='replace-with-your-local-application-password'
export AETHER_FLYWAY_PASSWORD='replace-with-your-local-migration-password'
```

Run validation and start the API:

```bash
./gradlew clean test
./gradlew bootRun
```

The default Spring profile is `local`.

## Local endpoints

| Endpoint | Purpose |
|---|---|
| `GET /actuator/health` | Overall process health |
| `GET /actuator/health/liveness` | Liveness probe |
| `GET /actuator/health/readiness` | Readiness probe, including database connectivity |
| `/swagger-ui.html` | Local interactive OpenAPI documentation |
| `/v3/api-docs` | OpenAPI specification |

API documentation is disabled in the `prod` profile. Only the safe Actuator `health` and `info` endpoints are exposed over HTTP by this foundation. Health details and components are never exposed anonymously.

## Production profile

Set `SPRING_PROFILES_ACTIVE=prod` and provide `AETHER_DB_URL`, `AETHER_DB_PASSWORD`, and `AETHER_FLYWAY_PASSWORD` through the deployment secret manager. The runtime role is `aether_app`; the migration role is `aether_migrator`. Keep the migration role separate from the application role. Do not put secrets in application YAML, source control, CI logs, or command history.

A production deployment still requires deployment-specific TLS, secrets management, network policy, database backup/restore, alerting, and authentication/authorization before any user or admin business APIs are exposed. Those are implemented in their relevant slices; this foundation does not pretend they are already complete.

## Delivery workflow

Each feature is delivered as a complete vertical slice on its own feature branch: define the problem and workflow, design only the required UX/domain/API/data changes, implement, validate, update `AETHER_HANDOFF.md` and architecture documentation, then commit and merge after validation.