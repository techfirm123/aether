-- The migrator role owns the schema.
-- The runtime application role receives no DDL privileges.

CREATE SCHEMA IF NOT EXISTS aether;

GRANT USAGE ON SCHEMA aether TO aether_app;

-- Future tables created by the Flyway migration role
-- are usable by the API runtime role.

ALTER DEFAULT PRIVILEGES IN SCHEMA aether
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO aether_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA aether
    GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO aether_app;