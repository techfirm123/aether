# AETHER — Platform Foundation Architecture

## 1. Architectural shape

AETHER starts with a modular-monolith backend. The mobile application and Admin Console are separate clients. The API owns identity, personal data, assistant orchestration and action boundaries. Platform analytics are logically isolated from personal content.

```text
React Native Mobile (Android-first)     Next.js Admin Console (future)
                 \                         /
                  \                       /
                   AETHER Spring Boot API
                     |             |
              Personal domain   Platform analytics
                     |             |
                   PostgreSQL (separate schemas/permissions as slices evolve)
```

This is a logical boundary; the first slice does not create speculative modules or schemas for capabilities that are not implemented.

## 2. Backend foundation conventions

- Root Java package: `com.aether.platform`.
- REST APIs will use the `/api/v1` prefix.
- Internal and operational endpoints use Spring Boot Actuator and remain limited to health and application info in the initial foundation.
- API errors use RFC 9457 Problem Details with stable application error codes and a correlation ID.
- Every HTTP request gets a correlation ID. A caller-supplied value is reused only when it is a canonical UUID; otherwise a new UUID is generated.
- Correlation IDs are returned in `X-Correlation-ID` and placed in SLF4J MDC.
- Secrets are supplied using environment variables or a deployment secret manager, never committed.
- Flyway is the only schema-change mechanism. Automatic SQL initialization is disabled.
- Runtime database credentials are separate from migration credentials; the application role receives no schema DDL privileges.
- Local and production profiles use PostgreSQL. The test profile uses an isolated in-memory H2 database and disables Flyway; PostgreSQL migrations must also be smoke-tested against local PostgreSQL.

## 3. Health and readiness

- Liveness checks whether the process is alive and must not depend on an external database.
- Readiness includes application readiness and the database health indicator.
- Health component details are hidden from anonymous callers.
- Only `health` and `info` are exposed over HTTP.

## 4. Logging and diagnostics

- Console logs use Logstash-compatible JSON.
- Request correlation IDs are attached through MDC.
- Do not log authorization headers, credentials, conversation text, memory content, notification content or document contents.
- Unexpected exceptions are logged with a correlation ID; API responses return a generic server-error detail.

## 5. Security boundaries

This foundation has no user or admin business endpoints and therefore does not add placeholder authentication. Identity and authorization are implemented before those endpoints are introduced. Production deployments must restrict network access to operational endpoints and use TLS, secrets management and controlled ingress.

## 6. Database approach

The default local schema is `aether`, managed by Flyway. PostgreSQL is the target database. pgvector is intentionally deferred until the memory slice defines its actual embedding and retrieval requirements. The test H2 database is only a fast context-test dependency; it is not a substitute for PostgreSQL migration validation.

## 7. Future modular boundaries

As features are introduced, prefer packages/modules around capability domains such as `identity`, `conversation`, `memory`, `context`, `organization`, `information`, `proactive`, `actions`, `analytics`, and `admin`. Introduce a boundary when a complete vertical slice requires it; do not create empty speculative subsystems now.