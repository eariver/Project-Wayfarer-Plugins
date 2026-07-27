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
- The merged alpha.2 PR A source implements the internal MariaDB/Hikari/Flyway lifecycle and
  isolated `mariadb:11.8` migration tests. Persistence shutdown drain
  ordering is automated-test covered after the fix: intake closes independently, accepted work
  drains before Hikari close, and timeout/interruption remain non-clean. Isolated Paper runtime
  evidence remains pending.
  MariaDB and Migration health are `UP` only after pool connectivity and Flyway validation/
  migration succeed; failures are `DOWN` and stop service publication.
- The alpha.2 PR B source implements additive V002, durable audit, player identity snapshots,
  common item identity, and required repositories. Local Docker was stopped, so PR B MariaDB
  integration is not claimed locally and GitHub Actions is mandatory authority. Paper runtime
  evidence remains pending.
- PR B correction separates Identity `OPEN`/`CLOSING`/`CLOSED`, defers clean Identity health until
  database drain finalization, adds durable player-upsert failure audit, and makes the configured
  Core server ID the sole audit persistence authority. Paper shutdown/runtime evidence remains
  pending.
- Audit retention scheduling is pending. A durable shutdown-timeout record is not guaranteed
  after database intake closes; health and sanitized logging remain the evidence. Main/Frontier
  PDC adapters remain pending.
- Transaction and reconcile repositories, optimistic claims, timeout-to-`UNKNOWN`, explicit
  reconcile, idempotency, duplicate-effect prevention, and restart recovery are implemented and
  automated-test covered. Concrete provider execution and Paper runtime evidence remain blocked
  or pending as described below.
- Public `WayfarerDatabase` remains unavailable as a JDK-only marker. Its pre-alpha JDBC-typed
  methods were removed because no accepted consumer exists; ADR 0005 records that incompatible
  governance decision. Any future opaque asynchronous contract requires a separate decision.
  Downstream plugins receive no JDBC `Connection`, Hikari, or Flyway implementation.
- The alpha.3 branch implements internal Redis cache/lock/message/idempotency assistance,
  explicit outage/reconnect health, main-thread rejection, and a bounded shutdown. Commit-pinned
  isolated Redis CI passed; Paper runtime evidence remains pending.
- The executor now has an immediate-rejection bounded queue and the task bridge validates
  immutable JDK-only data. Runtime queue-pressure/tick evidence remains pending.
- The alpha.4 provider-independent transaction engine, V003 repository/history, reconcile path,
  fixture SPI, and admin handlers are implemented. Concrete RedisEconomy/Vault invocation remains
  blocked by ADR 0006 because its safe thread/timeout/reference contract is not immutable authority.
- Incomplete and interrupted shutdown remain `DOWN` in Executor health even after lifecycle
  state becomes `DISABLED`.
- Permission-denial events use durable audit when audit is enabled. Shutdown-timeout durable
  persistence remains unguaranteed and must not bypass the drain gate.
- `V0.0.1` includes Core only; Main and Frontier gameplay are not included.
- The EliteMobs–MVI adapter is not authorized.
- Redis is not a persistent gameplay or inventory authority.
- Waymark operations cannot claim unconditional exactly-once across an external provider.
- Concrete provider API/thread/timeout/reference behavior remains an open authority gate under
  ADR 0006; the provider-independent transaction engine does not relax it.
- Hot reload/PlugMan-style reload is unsupported.
- Project placement, migration execution, configuration, permission application, server restart,
  runtime acceptance, Roadmap Order completion, and stable requirements clearance remain pending.

Update this file at every candidate; do not remove a limitation without commit-pinned evidence.
