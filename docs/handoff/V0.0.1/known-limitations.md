# V0.0.1 Known Limitations

- Alpha.1 isolated runtime evidence is complete and indexed at
  `docs/testing/evidence/V0.0.1-alpha.1-runtime-evidence.md`.
- Runtime observation passed for default-placeholder fail-closed, explicit server-ID startup,
  enable/disable, restart, console health, player permission, ServicesManager lookup, secret
  redaction, graceful shutdown, confirmed forced termination, incomplete shutdown, and
  Core-only final smoke.
- Runtime directly observed `GRACEFUL`, `FORCED_TERMINATED`, and `INCOMPLETE`. `INTERRUPTED` is
  covered by unit tests but was not directly observed in Paper runtime; all four states must not
  be described as runtime-tested.
- Four Harness incidents are retained in the evidence index: malformed Phase H config,
  Phase J marker mismatch, Player Checkpoint stdin loss with Project Owner-approved forced
  recovery, and Final Smoke newline-regex mismatch. Corrected reruns or canonical evidence passed,
  so these incidents do not invalidate the product runtime result.
- The alpha.2 PR A source implements the internal MariaDB/Hikari/Flyway lifecycle and isolated
  `mariadb:11.8` migration tests. PR A remains Draft and unmerged. Persistence shutdown drain
  ordering is automated-test covered after the fix: intake closes independently, accepted work
  drains before Hikari close, and timeout/interruption remain non-clean. Isolated Paper runtime
  evidence remains pending.
  MariaDB and Migration health are `UP` only after pool connectivity and Flyway validation/
  migration succeed; failures are `DOWN` and stop service publication.
- Durable audit persistence and player/item identity remain pending alpha.2 PR B. The transaction
  repository, reconcile repository, optimistic locking behavior, and repository restart recovery
  are also not implemented by PR A.
- The public `WayfarerDatabase` stub remains unavailable. PR A adds only an internal JDBC boundary;
  downstream plugins receive no JDBC `Connection`, Hikari, or Flyway implementation.
- Redis remains pending alpha.3 and Waymark remains pending alpha.4.
- The alpha.1 executor uses a fixed pool. Bounded queue/backpressure is deferred to alpha.3.
- Incomplete and interrupted shutdown remain `DOWN` in Executor health even after lifecycle
  state becomes `DISABLED`.
- Health permission-denial and shutdown-timeout observations use sanitized operational logging;
  durable audit persistence is deferred to alpha.2.
- `V0.0.1` includes Core only; Main and Frontier gameplay are not included.
- The EliteMobs–MVI adapter is not authorized.
- Redis is not a persistent gameplay or inventory authority.
- Waymark operations cannot claim unconditional exactly-once across an external provider.
- Provider API/thread behavior remains an open gate until the transaction slice.
- Hot reload/PlugMan-style reload is unsupported.
- Project placement, migration execution, configuration, permission application, server restart,
  runtime acceptance, Roadmap Order completion, and stable requirements clearance remain pending.

Update this file at every candidate; do not remove a limitation without commit-pinned evidence.
