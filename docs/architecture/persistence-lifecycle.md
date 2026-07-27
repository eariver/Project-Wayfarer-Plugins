# Core Persistence Lifecycle

## Scope and authority

The alpha.2 PR A persistence foundation is internal to `wayfarer-core`. MariaDB remains the
authority for approved Wayfarer-owned durable domains. Redis, MVI, Waymark, normal inventories,
Main, and Frontier authority are unchanged.

The isolated integration fixture uses `mariadb:11.8`, matching the immutable Project mainline
authority `eariver/Project_Wayfarer` `infrastructure/compose.yml` blob
`42e62979c8c70290fae78c0731ae628e940d74e9`. It never connects to a Project Runtime database.

## Enable and disable order

Enable is fail-closed:

```text
Config and secrets
→ Managed executor
→ Hikari pool creation and connection validation on the executor
→ Flyway pre-validation, migrate, post-validation, and schema-info check on the executor
→ Bukkit service publication
```

Disable separates database intake from accepted work. Its effective order is:

```text
Services unpublished
→ Database intake closed
→ Accepted database work drained while the executor and Hikari pool remain available
→ Migration lifecycle released
→ Hikari pool closed
→ Executor shutdown
→ Config secrets released
```

The persistence gate is independent of the synchronized lifecycle state. Submission acquires one
idempotent permit, and worker completion or executor submission rejection releases it exactly
once. Work accepted before intake closes can therefore run while the lifecycle is `STOPPING`;
work submitted afterward is rejected before connection acquisition.

Drain uses the configured shutdown timeout and reports `DRAINED`, `TIMED_OUT`, or `INTERRUPTED`
with the remaining in-flight count. `DRAINED` permits the normal MariaDB `DISABLED` transition.
Timeout and interruption keep MariaDB health `DOWN`, emit only a sanitized warning, and never
claim a clean drain; interruption also restores the thread interrupt status. Cleanup remains
bounded and continues through migration release, pool close, and the executor's independently
reported graceful, forced, incomplete, or interrupted result.

Pool, migration, gate, and runtime close operations are idempotent. A pool/connect/migration
failure marks the applicable health component `DOWN`, prevents service publication, closes
already-created resources, and surfaces only a sanitized failure category.

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
The worker path does not call the lifecycle coordinator or acquire its synchronization lock.
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
silently skipped. The suite includes a one-thread queued-operation regression and a real
MariaDB test proving both accepted effects commit before disable closes Hikari.
