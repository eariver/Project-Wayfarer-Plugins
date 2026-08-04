# V0.0.2 Migration and Compatibility

| Component | Current level | Compatibility state |
|---|---|---|
| Core API | V0.0.1 | Reused unchanged; public API compatibility passes |
| Core migrations | V001–V003 | Immutable; no applied migration is edited |
| Main config | current source config | Main V004 is the current reissue migration level |
| Main migrations | V001–V004 | Separate Main history; forward-only |
| Frontier config | current source config | Current source migration level |
| Frontier migrations | V001–V002 | Separate Frontier history; forward-only |

Main and Frontier require Core `>=0.0.1 <0.1.0`, have no mutual dependency, and do not expose
JDBC/Hikari/Flyway or server types through the public API. Core has no dependency on either
gameplay module.

Compatibility boundaries:

- Core V0.0.1 is reused; Core V001–V003 are immutable.
- Main is current through V004; Frontier remains at its current source migration level.
- Main and Frontier retain separate histories and do not down-migrate.
- Removing a JAR is not schema rollback.
- Paid reissue, Pending Delivery, `UNKNOWN`, and durable recovery are state-aware; disabling a
  module does not delete its records.
- The old broad permission nodes are inactive and are not released compatibility promises.
- Project rollback, Runtime configuration, migration execution, and inventory/profile state are
  Project/MVI-owned.

Do not manually edit `flyway_schema_history`, apply these migrations to Project databases, or
treat local/CI tests as Runtime authorization.
