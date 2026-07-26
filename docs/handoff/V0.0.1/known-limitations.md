# V0.0.1 Known Limitations

- Lifecycle Foundation implementation and automated tests are complete on the alpha.1 Draft PR;
  isolated runtime evidence is not started.
- MariaDB/Hikari/Flyway, Redis, Waymark, transaction, identity, and audit persistence are not
  implemented in alpha.1. Health reports these dependencies as `UNKNOWN`, never falsely `UP`.
- The alpha.1 executor uses a fixed pool. Bounded queue/backpressure is deferred to alpha.3.
- Executor shutdown distinguishes graceful completion, confirmed forced termination,
  incomplete termination, and interruption. The configured timeout applies to each of the
  graceful and forced phases, for a maximum block of approximately twice that value.
- Incomplete and interrupted shutdown remain `DOWN` in Executor health even after lifecycle
  state becomes `DISABLED`. Runtime timeout observation remains pending.
- Health permission-denial and shutdown-timeout observations use sanitized operational logging;
  durable audit persistence is deferred to alpha.2.
- `V0.0.1` includes Core only; Main and Frontier gameplay are not included.
- The EliteMobs–MVI adapter is not authorized.
- Redis is not a persistent gameplay or inventory authority.
- Waymark operations cannot claim unconditional exactly-once across an external provider.
- Provider API/thread behavior remains an open gate until the transaction slice.
- Hot reload/PlugMan-style reload is unsupported.
- Paper enable/disable, restart, command, permission, service lookup, and log-redaction behavior
  still require repository-external isolated test-server evidence. Default-placeholder startup,
  explicit server-ID startup, and shutdown timeout behavior are included in that pending evidence.
- Project placement, migration execution, configuration, permission application, server restart,
  runtime acceptance, and Roadmap Order completion remain outside this repository task.

Update this file at every candidate; do not remove a limitation without commit-pinned evidence.
