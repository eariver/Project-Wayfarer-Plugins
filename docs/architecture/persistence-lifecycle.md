# Core Persistence Lifecycle

## Scope and authority

The alpha.2 PR A persistence foundation is internal to `wayfarer-core`. MariaDB remains the
authority for approved Wayfarer-owned durable domains. Redis, MVI, Waymark, normal inventories,
Main, and Frontier authority are unchanged.

The isolated integration fixture uses `mariadb:11.8`, matching the Project mainline authority at
`eariver/Project_Wayfarer` `infrastructure/compose.yml`. It never connects to a Project Runtime
database.

## Enable and disable order

Enable is fail-closed:

```text
Config and secrets
→ Managed executor
→ Hikari pool creation and connection validation on the executor
→ Flyway pre-validation, migrate, post-validation, and schema-info check on the executor
→ Bukkit service publication
```

Disable and failed-start cleanup use the exact reverse resource order:

```text
Services unpublished
→ Migration lifecycle released
→ Hikari pool closed
→ Executor shutdown
→ Config secrets released
```

Pool and migration close operations are idempotent. A pool/connect/migration failure marks the
applicable health component `DOWN`, prevents service publication, closes already-created
resources, and surfaces only a sanitized failure category.

## Hikari and Flyway boundary

Hikari maps the typed version-1 Core configuration to a bounded, fail-fast pool with
`autoCommit=true`, `READ_COMMITTED`, UTC session time, disabled MBeans, and a sanitized
`Wayfarer-Core-*` pool name. Flyway accepts canonical `classpath:` locations only. The released
Core migration is immutable:

```text
plugins/wayfarer-core/src/main/resources/db/migration/core/V001__core_schema.sql
SHA-256 59035d3bf0ee9f11e2a6756138fa55f331dc79546778c473bacbde887a894840
```

No production V002 migration is added by PR A.

## JDBC threading and API boundary

Internal JDBC work is submitted to the managed database executor. A Paper-independent
`ThreadContext` guard rejects any attempted JDBC connection acquisition from the main thread.
Reads close their connection with try-with-resources. Transactions explicitly select
`READ_COMMITTED`, disable auto-commit, commit success, and roll back failure. SQL diagnostics are
converted to sanitized internal exceptions.

The existing public `WayfarerDatabase` stub is intentionally not implemented or published.
`WayfarerServices.database()` remains unavailable, so downstream plugins receive no JDBC
`Connection`, Hikari, Flyway, or implementation class.

## Automated gate

`libraries/wayfarer-testkit` owns the Testcontainers MariaDB fixture. Production modules do not
depend on testkit. `:plugins:wayfarer-core:mariaDbIntegrationTest` starts uniquely named isolated
databases, fails when Docker is unavailable, and is a mandatory dependency of `check`; it is not
silently skipped.
