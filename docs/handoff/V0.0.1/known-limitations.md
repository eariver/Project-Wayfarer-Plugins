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
  now covers empty migration, repeated startup, MariaDB fail-closed, migration-checksum
  fail-closed, and clean disable. Paper in-flight drain timeout/interruption injection remains
  pending and must not be inferred from the clean case.
  MariaDB and Migration health are `UP` only after pool connectivity and Flyway validation/
  migration succeed; failures are `DOWN` and stop service publication.
- The alpha.2 PR B source implements additive V002, durable audit, player identity snapshots,
  common item identity, and required repositories. Local Docker was stopped, so PR B MariaDB
  integration is not claimed locally and GitHub Actions is mandatory authority. The rc.1 Paper
  probe passed durable audit and item identity create/find/validate; real PlayerJoin identity
  remains pending client acceptance.
- PR B correction separates Identity `OPEN`/`CLOSING`/`CLOSED`, defers clean Identity health until
  database drain finalization, adds durable player-upsert failure audit, and makes the configured
  Core server ID the sole audit persistence authority. Paper shutdown/runtime evidence remains
  pending.
- Audit retention scheduling is pending. A durable shutdown-timeout record is not guaranteed
  after database intake closes; health and sanitized logging remain the evidence. Main/Frontier
  PDC adapters remain pending.
- Transaction and reconcile repositories now use exact full-input idempotency, separate durable
  debit/refund effect identities, bounded recovery claims, atomic transaction history, and
  startup/manual recovery. Corrected alpha.4 CI `30354268891` passed; concrete provider behavior
  remains blocked by ADR 0006.
- Public `WayfarerDatabase` remains unavailable as a JDK-only marker. Its pre-alpha JDBC-typed
  methods were removed because no accepted consumer exists; ADR 0005 records that incompatible
  governance decision. Any future opaque asynchronous contract requires a separate decision.
  Downstream plugins receive no JDBC `Connection`, Hikari, or Flyway implementation.
- The alpha.3 branch implements internal Redis cache/lock/message/idempotency assistance,
  explicit outage/reconnect health, main-thread rejection, and a bounded shutdown. Commit-pinned
  isolated Redis CI passed. The rc.1 Paper run observed Redis health `DOWN` during outage and `UP`
  after reconnect.
- The executor now has an immediate-rejection bounded queue and the task bridge validates
  immutable JDK-only data. The Paper probe verified worker and main-thread bridge boundaries and
  recorded one 20 TPS / 13.2 ms tick observation. Runtime queue-pressure and shutdown-timeout
  injection remain covered only by automated suites.
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
- The JDK-only provider discovery seam and worker-thread fixture probe/recovery passed automated
  tests. Concrete provider API/thread/timeout/crash-reference behavior remains an open authority
  gate; external plugin class identity/load order is not inferred.
- The successful rc.1 pre-client evidence is limited to client-independent scope. Harness
  calibration and two corrected Paper classloader defects are indexed in
  `docs/testing/evidence/V0.0.1-rc.1-preclient-headless.md`; unsuccessful runs are not evidence of
  a pass.
- Real PlayerJoin/reconnect identity, player-visible permissions/output/responsiveness, and any
  authorized concrete Waymark balance/debit/refund behavior remain pending the client acceptance
  package.
- Hot reload/PlugMan-style reload is unsupported.
- Project placement, migration execution, configuration, permission application, server restart,
  runtime acceptance, Roadmap Order completion, and stable requirements clearance remain pending.

Update this file at every candidate; do not remove a limitation without commit-pinned evidence.
