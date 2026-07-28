# Requirement Traceability

- Release gate: BLOCKED
- Acceptance units: 245

The Project mainline requirement snapshot is the authority for source sections. A documentation
skeleton does not satisfy the underlying implementation or test requirement. `Passed` and
`Runtime test passed` require commit-pinned implementation, automated results, and runtime
evidence as applicable.

In the table below, `alpha.1 runtime evidence` refers to
`docs/testing/evidence/V0.0.1-alpha.1-runtime-evidence.md`.

| Requirement ID | Requirement | Source section | Implementation | Automated test | Pre-release | Runtime evidence | Status | Notes |
|---|---|---|---|---|---|---|---|---|
| GOV-001 | Preserve Project authority references | 5 | `source.md` | Reference review | N/A | N/A | Implemented | Commit/blob pinned |
| GOV-002 | Core-only initial artifact scope | 3, 6.1 | Workflows; plan | Scope assertions | V0.0.x | N/A | Implemented | Runtime release not executed |
| GOV-003 | Main excluded from V0.0.1 release | 3, 6.1 | Workflows | Forbidden-asset assertion | V0.0.x | N/A | Implemented | Build skeleton only |
| GOV-004 | Frontier excluded from V0.0.1 release | 3, 6.1 | Workflows | Forbidden-asset assertion | V0.0.x | N/A | Implemented | Build skeleton only |
| GOV-005 | Conditional adapter requires `ADAPTER_REQUIRED` | 2.3, 6.1 | `AGENTS.md`; plans | Repository inspection | All | N/A | Implemented | Not authorized |
| GOV-006 | Core does not depend on Main/Frontier | 6.2 | Gradle boundary | Dependency test planned | beta.1 | N/A | Not started | Acceptance test pending |
| GOV-007 | Main and Frontier do not interdepend | 6.2 | Gradle boundary | Dependency test planned | beta.1 | N/A | Not started | Acceptance test pending |
| GOV-008 | Preserve MariaDB/Redis/MVI/Waymark authority | 6.3 | Architecture docs | Boundary tests planned | beta.1 | Pending | Not started | Runtime evidence pending |
| GOV-009 | Do not alter Project Runtime | 6.5, 14 | `AGENTS.md`; process | Forbidden tracking check | All | N/A | Implemented | Repository-only work |
| GOV-010 | No cross-backend item transfer | 6.3, 14 | Architecture boundary | Domain inspection planned | beta.1 | Pending | Not started | Core must remain item-independent |
| FND-001 | Java 25 baseline | 7 | Gradle toolchain | Build validation | V0.0.1-alpha.1 / beta.1 | alpha.1 runtime evidence | In progress | Java 25 build and runtime observed; full acceptance pending |
| FND-002 | Gradle 9.6.1 baseline | 7 | Wrapper properties | Wrapper validation | beta.1 | N/A | In progress | Checksum evidence pending |
| FND-003 | Kotlin DSL build | 7 | `*.gradle.kts` | Build validation | beta.1 | N/A | In progress | Full acceptance pending |
| FND-004 | Paper API 1.21.11 baseline | 7 | Version catalog | Dependency inspection | V0.0.1-alpha.1 / beta.1 | alpha.1 runtime evidence | In progress | Paper 1.21.11 observed; full acceptance remains pending |
| FND-005 | Group/package `io.github.eariver.wayfarer` | 7 | Build/source | Package inspection | beta.1 | N/A | In progress | Full artifact inspection pending |
| FND-006 | Gradle Wrapper tracked | 7 | `gradlew*`; `gradle/wrapper` | Git inspection | beta.1 | N/A | In progress | Clean clone test pending |
| FND-007 | Wrapper version verified | 7 | Wrapper properties | Wrapper test | beta.1 | N/A | Not started | Evidence pending |
| FND-008 | Wrapper checksum verified | 7 | Pending verification metadata | Checksum test | beta.1 | N/A | Not started | Evidence pending |
| FND-009 | Clean `check` succeeds | 7 | Gradle | Clean build | V0.0.1-alpha.1 / beta.1 | N/A | In progress | Alpha.1 corrected-head clean check passed; later release recheck pending |
| FND-010 | Clean `assemble` succeeds | 7 | Gradle | Clean build | V0.0.1-alpha.1 / beta.1 | N/A | In progress | Alpha.1 corrected-head clean assemble passed; later release recheck pending |
| FND-011 | CI uses Java 25 | 7 | `ci.yml` | CI run | V0.0.1-alpha.1 / beta.1 | N/A | In progress | Alpha.1 corrected-head CI passed; full acceptance pending |
| FND-012 | Configuration cache compatible | 7 | Gradle configuration | Cache check/assemble | V0.0.1-alpha.1 / beta.1 | N/A | In progress | Alpha.1 cache check/assemble passed; later release recheck pending |
| FND-013 | Versions centralized | 7 | Version catalog/properties | Dependency inspection | beta.1 | N/A | In progress | Full inspection pending |
| FND-014 | Compiler warnings fail build | 7 | `-Xlint:all -Werror` | Compile test | V0.0.1-alpha.1 / beta.1 | N/A | In progress | Alpha.1 compiler warnings were zero; later source coverage remains |
| FND-015 | Reproducible artifact | 7 | Reproducible Shadow archive | Two clean build SHA-256 comparison | V0.0.1-alpha.1 / beta.1 | N/A | In progress | Alpha.1 same-source local rebuild hashes matched; later release recheck pending |
| FND-016 | Source commit maps to artifact hash | 7, 12 | Release manifest | Packaging test | V0.0.1-alpha.1 | alpha.1 runtime evidence | Runtime test passed | Annotated tag source and released Core JAR hash verified |
| FND-017 | API class identity preserved | 7, 10.1 | API contracts; Core service factory | `CoreRuntimeServicesTest` | V0.0.1-alpha.1 / beta.1 | alpha.1 runtime evidence | In progress | ServicesManager lookup and class isolation observed; full beta compatibility pending |
| FND-018 | Runtime JAR excludes tests/secrets/runtime data | 7 | Core Shadow archive | JAR entry/config inspection | V0.0.1-alpha.1 / beta.1 | alpha.1 runtime evidence | In progress | Alpha.1 release JAR packaging passed; full beta scan pending |
| FND-019 | Generated JAR is not tracked | 7, 15 | `.gitignore`; policy | Git inspection | All | N/A | Implemented | No JAR tracked |
| FND-020 | License supplied | 7, 12 | `LICENSE` | Package inspection | beta.1 | N/A | In progress | Release inclusion pending |
| FND-021 | Third-party notice supplied | 7, 12 | Pending | Package inspection | beta.1 | N/A | Not started | Document pending |
| FND-022 | Secrets/runtime/world/DB/log/cache not tracked | 7, 14 | `.gitignore`; policy | Git inspection | All | N/A | Implemented | Recheck each release |
| LIF-001 | Typed config validation | 8.1 | `CoreConfig`; `CoreConfigLoader` | `CoreConfigLoaderTest` | V0.0.1-alpha.1 | alpha.1 runtime evidence | Runtime test passed | Valid and unsupported config runtime paths observed |
| LIF-002 | Environment secret resolution | 8.1 | `EnvironmentSecretResolver`; `SecretValue` | Common/config secret tests | V0.0.1-alpha.1 | alpha.1 runtime evidence | Runtime test passed | Missing and resolved secret validation paths observed without disclosure |
| LIF-003 | MariaDB pool lifecycle | 8.1 | `MariaDbPool`; persistence gate; `CoreRuntime` drain lifecycle | Pool, queued-drain, and MariaDB integration tests | alpha.2 | Pending | Automated test passed | Hikari mapping, connectivity, accepted-work drain before close, double-disable, outage, and cleanup passed; Paper runtime pending |
| LIF-004 | Flyway validate | 8.1 | `MigrationLifecycle` pre/post validation | Empty/repeated MariaDB migration tests | alpha.2 | Pending | Automated test passed | Pending migrations are allowed only for pre-validation; post-validation passed |
| LIF-005 | Flyway migrate | 8.1 | `MigrationLifecycle` | Empty/repeated MariaDB migration tests | alpha.2 | Pending | Automated test passed | Core V001 applied once on isolated MariaDB 11.8 |
| LIF-006 | Redis foundation | 8.1 | Redis slice | Integration test | alpha.3 | Pending | Not started | Implementation pending |
| LIF-007 | Redis health | 8.1 | Redis slice | Outage/reconnect test | alpha.3 | Pending | Not started | Implementation pending |
| LIF-008 | Waymark capability probe | 8.1 | Transaction slice | Provider fixture | alpha.4 | Pending | Not started | Provider contract gate |
| LIF-009 | Services registration | 8.1 | `BukkitServicePublisher`; `CoreRuntime` | ServicesManager/runtime tests | V0.0.1-alpha.1 | alpha.1 runtime evidence | Runtime test passed | ServicesManager lookup succeeded |
| LIF-010 | Services unregister | 8.1 | `BukkitServicePublisher`; service-first cleanup | Disable/idempotency and persistence-drain tests | V0.0.1-alpha.1 / alpha.2 | alpha.1 runtime evidence | Runtime test passed | Services unpublish before database intake closes; clean restart showed no duplicate registration |
| LIF-011 | Executor lifecycle | 8.1 | `ManagedExecutor`; explicit shutdown result; persistence gate | Graceful/forced/incomplete/interrupted and queued-drain tests | V0.0.1-alpha.1 / alpha.2 / alpha.3 | alpha.1 runtime evidence | In progress | Accepted DB work drains while executor remains live and then terminates gracefully; interrupted runtime and queue/backpressure remain pending |
| LIF-012 | Scheduler lifecycle | 8.1 | lifecycle-guarded `MainThreadDispatcher` | `DefaultWayfarerTasksTest` | alpha.1 | Pending | Automated test passed | Paper scheduler runtime pending |
| LIF-013 | Clean disable | 8.1 | `LifecycleCoordinator`; Identity close/quiesce/finalize; audit close; persistence drain; shutdown-result health | Lifecycle, queued-drain, identity-finalization, and runtime health tests | V0.0.1-alpha.1 / alpha.2 | alpha.1 runtime evidence | Runtime test passed | Accepted Identity work quiesces before audit close; clean health waits for the database drain; Paper persistence-drain evidence remains pending |
| LIF-014 | Timeout-bounded flush | 8.1 | Bounded Identity quiescence/finalization and persistence drain plus two-phase executor shutdown | Identity/persistence timeout and interrupt plus executor shutdown tests | V0.0.1-alpha.1 / alpha.2 | alpha.1 runtime evidence | Runtime test passed | Identity/persistence timeout/interruption are `DOWN` and non-clean; executor graceful and forced timeout phases observed |
| LIF-015 | Partial initialization cleanup | 8.1 | Reverse-order lifecycle resources | Failure injection tests | alpha.1 | Pending | Automated test passed | Runtime failure injection pending |
| LIF-016 | Fail-closed lifecycle | 8.1 | Explicit lifecycle state machine | Invalid/double/failure tests | V0.0.1-alpha.1 | alpha.1 runtime evidence | Runtime test passed | Placeholder, unsupported config, and missing secret failed closed |
| LIF-017 | Reject callbacks after disable | 8.1 | Double-guarded task bridge | Late callback test | alpha.1/3 | Pending | Automated test passed | Paper callback runtime pending |
| LIF-018 | Do not continue after migration failure | 8.1 | Fail-closed Core persistence lifecycle | Broken-migration failure injection | alpha.2 | Pending | Automated test passed | Services remained unpublished and Hikari closed; runtime evidence pending |
| API-001 | `WayfarerServices` contract | 8.2 | Typed metadata/services contract | Service/API identity tests | V0.0.1-alpha.1 / beta.1 | alpha.1 runtime evidence | In progress | Runtime service lookup passed; full beta contract remains pending |
| API-002 | `WayfarerDatabase` contract | 8.2 | JDK-only zero-method reserved marker; runtime unavailable; ADR 0005 | Full public API boundary scan and reflection check | beta.1 | Pending | In progress | JDBC-typed pre-alpha stub rejected and removed; accepted opaque beta contract requires a new decision |
| API-003 | `WayfarerAudit` contract | 8.2 | Async JDK API plus durable Core implementation | API boundary/unit/integration suite | beta.1 | Pending | In progress | Contract implemented; beta compatibility and Paper runtime pending |
| API-004 | `WayfarerTransactions` contract | 8.2 | `wayfarer-api` | API compatibility test | beta.1 | Pending | Not started | Existing stub not accepted |
| API-005 | `WayfarerWaymark` contract | 8.2 | `wayfarer-api` | API compatibility test | beta.1 | Pending | Not started | Existing stub not accepted |
| API-006 | `WayfarerItemIdentity` contract | 8.2 | ADR 0004 asynchronous JDK-only contract | API boundary and identity suites | beta.1 | Pending | In progress | Stub revised before accepted consumers; beta compatibility and runtime pending |
| API-007 | `WayfarerTasks` contract | 8.2 | `wayfarer-api` | API compatibility test | beta.1 | Pending | Not started | Existing stub not accepted |
| API-008 | `WayfarerHealth` contract | 8.2 | Timestamped snapshot/component contract | Health/API identity tests | V0.0.1-alpha.1 / beta.1 | alpha.1 runtime evidence | In progress | Health summary/component output observed; full beta contract remains pending |
| API-009 | Service lookup/thread/completion/timeout semantics documented | 8.2 | API design pending | Contract tests | beta.1 | Pending | Not started | Implementation pending |
| API-010 | Idempotency/disable/health semantics documented | 8.2 | API design pending | Contract tests | beta.1 | Pending | Not started | Implementation pending |
| API-011 | Item/transaction/Waymark/audit/task semantics documented | 8.2 | API design pending | Contract tests | beta.1 | Pending | Not started | Implementation pending |
| API-012 | Hikari not exposed | 8.2 | API boundary | Leak test | beta.1 | N/A | Not started | Inspection pending |
| API-013 | Flyway not exposed | 8.2 | API boundary | Leak test | beta.1 | N/A | Not started | Inspection pending |
| API-014 | Lettuce not exposed | 8.2 | API boundary | Leak test | beta.1 | N/A | Not started | Inspection pending |
| API-015 | JDBC Connection/plugin implementation not exposed | 8.2 | API boundary | Leak test | beta.1 | N/A | Not started | Inspection pending |
| API-016 | Paper Event/Player/ItemStack not exposed | 8.2 | API boundary | Leak test | beta.1 | N/A | Not started | Inspection pending |
| API-017 | Server gameplay domain not exposed | 8.2 | API boundary | Leak test | beta.1 | N/A | Not started | Inspection pending |
| CFG-001 | Config version | 8.3 | `config-version: 1`; loader | Missing/unsupported tests | V0.0.1-alpha.1 | alpha.1 runtime evidence | Runtime test passed | Version 1 accepted; version 2 failed closed |
| CFG-002 | Server ID | 8.3 | Typed validated ID with reserved-placeholder rejection | Missing/blank/format/placeholder/explicit tests | V0.0.1-alpha.1 | alpha.1 runtime evidence | Runtime test passed | Default placeholder rejected; explicit `wayfarer-test` accepted |
| CFG-003 | MariaDB connection settings | 8.3 | Typed Hikari mapping and secret resolution | Config/pool/integration tests | alpha.2 | alpha.1 runtime evidence | Automated test passed | Active mapping tested; Project Runtime configuration remains unchanged |
| CFG-004 | Redis connection settings | 8.3 | Typed inactive alpha.1 settings | Validation/missing-secret test | alpha.3 | alpha.1 runtime evidence | In progress | Disabled config accepted and missing reference failed closed; connection deferred to alpha.3 |
| CFG-005 | Migration settings | 8.3 | Typed canonical classpath locations and dependency guard | Config/location/migration tests | alpha.2 | alpha.1 runtime evidence | Automated test passed | Empty, traversal, and non-classpath locations fail closed |
| CFG-006 | Waymark provider settings | 8.3 | Typed inactive provider settings | Config validation suite | alpha.4 | alpha.1 runtime evidence | In progress | Disabled config accepted; capability probe deferred to alpha.4 |
| CFG-007 | Executor settings | 8.3 | Typed thread count/name | Range/name tests | V0.0.1-alpha.1 / alpha.3 | alpha.1 runtime evidence | In progress | Thread and timeout behavior observed; queue settings deferred to alpha.3 |
| CFG-008 | Audit settings | 8.3 | Typed durable-audit dependency gate | Enabled/disabled/dependency tests | alpha.2 | Pending | Automated test passed | Version remains 1; true requires MariaDB and migration; sample false |
| CFG-009 | Health settings | 8.3 | Typed player detail flag | Valid config/command tests | V0.0.1-alpha.1 | alpha.1 runtime evidence | Runtime test passed | Player status rows shown while parenthetical internal details were suppressed |
| CFG-010 | Shutdown timeout settings | 8.3 | Typed bounded duration | Range/timeout tests | V0.0.1-alpha.1 | alpha.1 runtime evidence | Runtime test passed | One-second graceful and forced phases observed |
| CFG-011 | Sanitized sample | 8.3, 12 | Core `config.yml`; handoff | Secret/redaction tests | beta.1 | alpha.1 runtime evidence | In progress | Sanitized runtime config observed; full beta asset scan pending |
| CFG-012 | No secret in Git/YAML | 8.3 | Environment references only | Secret/redaction tests | beta.1 | alpha.1 runtime evidence | In progress | Environment references used in alpha.1; full history scan pending |
| CFG-013 | No secret in logs | 8.3 | Sanitized plugin diagnostics | Command/health redaction tests | beta.1 | alpha.1 runtime evidence | In progress | Runtime secret hit count was zero; full beta review pending |
| CFG-014 | No secret in audit | 8.3 | Sanitized operational event codes | Denial/failure event tests | beta.1 | Pending | Automated test passed | Durable audit deferred |
| CFG-015 | No secret in exceptions | 8.3 | Safe config/lifecycle exceptions | Failure/redaction tests | beta.1 | alpha.1 runtime evidence | In progress | Surfaced runtime validation diagnostic was sanitized; full beta review pending |
| DB-001 | Core owns only `wf_core_*` | 8.4 | Existing immutable Core V001 | Isolated schema inspection | alpha.2 | Pending | Automated test passed | Only Flyway history, `wf_core_transaction`, and `wf_core_audit` created |
| DB-002 | Transaction domain | 8.4 | Core migration/repository | Integration test | alpha.2/4 | Pending | Not started | Implementation pending |
| DB-003 | Transaction event/history domain | 8.4 | Core migration/repository | Integration test | alpha.2/4 | Pending | Not started | Implementation pending |
| DB-004 | Audit domain | 8.4 | V001 table plus durable repository | Idempotency/redaction/restart integration suite | alpha.2 | Pending | In progress | Implementation and test source complete; GitHub Actions authority pending |
| DB-005 | Player identity domain | 8.4 | Additive V002 and newer-only repository | Repository/restart integration suite | alpha.2 | Pending | In progress | Implementation and test source complete; GitHub Actions authority pending |
| DB-006 | Item identity domain | 8.4 | Additive V002 and async repository | Create/find/validate/restart suite | alpha.2 | Pending | In progress | Implementation and test source complete; GitHub Actions authority pending |
| DB-007 | Reconcile state domain | 8.4 | Core migration/repository | Recovery test | alpha.2/4 | Pending | Not started | Implementation pending |
| DB-008 | Empty DB migration | 8.4 | `MigrationLifecycle`; MariaDB testkit fixture | `mariaDbIntegrationTest` | alpha.2 | Pending | Automated test passed | Empty isolated MariaDB 11.8 migrated successfully |
| DB-009 | Additive migration | 8.4 | V002 adds only two Core identity tables | Empty/V001-upgrade/repeated tests | alpha.2 | Pending | In progress | V001 unchanged; integration source compiled; CI execution pending |
| DB-010 | Applied migration immutable | 8.4 | V001 unchanged | Released/current/classpath SHA-256 checks | alpha.2+ | N/A | Automated test passed | All checked bytes match `59035d3b...4840`; no new production migration |
| DB-011 | UUID authority | 8.4 | Canonical UUID repositories and raw-claim gate | Unit/repository tests | alpha.2 | Pending | In progress | Unit validation passed; MariaDB CI pending |
| DB-012 | UTC timestamps | 8.4 | Eight `TIMESTAMP(3)` columns, UTC pool, repository mapping | Schema/session/mapping inspection | alpha.2 | Pending | In progress | Existing UTC tests passed; V002/repository CI pending |
| DB-013 | Unique constraints | 8.4 | Existing Core V001 | Duplicate idempotency/event tests | alpha.2 | Pending | Automated test passed | Current transaction and audit unique constraints enforced |
| DB-014 | Optimistic locking | 8.4 | Player newer-only lock increment; item lock foundation | Concurrency/repository tests | alpha.2 | Pending | In progress | Identity implementation present; broader transaction locking pending |
| DB-015 | Required indexes | 8.4 | V001 indexes plus four V002 identity indexes | Information-schema inspection | alpha.2 | Pending | In progress | V002 integration source compiled; CI execution pending |
| DB-016 | JSON validation | 8.4 | Existing Core V001 | Transaction/audit invalid-JSON tests | alpha.2 | Pending | Automated test passed | Both current JSON check constraints enforced |
| DB-017 | Restart recovery | 8.4 | Durable audit/player/item repositories | Restart integration suite | alpha.2 | Pending | In progress | Identity/audit source complete; CI pending; transaction/reconcile later |
| DB-018 | Core does not create `wf_main_*` | 8.4 | Core-only migration location | Exact schema-set test | alpha.2 | Pending | Automated test passed | No `wf_main_*` table created |
| DB-019 | Core does not create `wf_frontier_*` | 8.4 | Core-only migration location | Exact schema-set test | alpha.2 | Pending | Automated test passed | No `wf_frontier_*` table created |
| TX-001 | Persist/recover PREPARED and DEBIT_PENDING | 8.5 | Transaction slice | State test | alpha.4 | Pending | Not started | Implementation pending |
| TX-002 | Persist/recover DEBITED | 8.5 | Transaction slice | State test | alpha.4 | Pending | Not started | Implementation pending |
| TX-003 | Persist/recover DOMAIN_COMMIT_PENDING and COMMITTED | 8.5 | Transaction slice | State test | alpha.4 | Pending | Not started | Implementation pending |
| TX-004 | Persist/recover REFUND_PENDING and REFUNDED | 8.5 | Transaction slice | State test | alpha.4 | Pending | Not started | Implementation pending |
| TX-005 | Persist/recover UNKNOWN and FAILED | 8.5 | Transaction slice | State test | alpha.4 | Pending | Not started | Implementation pending |
| TX-006 | Persist RECONCILED_COMMITTED | 8.5 | Transaction slice | Reconcile test | alpha.4 | Pending | Not started | Implementation pending |
| TX-007 | Persist RECONCILED_REFUNDED | 8.5 | Transaction slice | Reconcile test | alpha.4 | Pending | Not started | Implementation pending |
| TX-008 | Unique idempotency key | 8.5 | Transaction slice | Idempotency test | alpha.4 | Pending | Not started | Implementation pending |
| TX-009 | Prevent duplicate debit | 8.5 | Transaction slice | Concurrency/restart test | alpha.4 | Pending | Not started | Implementation pending |
| TX-010 | Prevent duplicate refund | 8.5 | Transaction slice | Concurrency/restart test | alpha.4 | Pending | Not started | Implementation pending |
| TX-011 | Detect crash windows | 8.5 | Transaction slice | Failure injection | alpha.4 | Pending | Not started | Implementation pending |
| TX-012 | Timeout behavior | 8.5 | Transaction slice | Timeout test | alpha.4 | Pending | Not started | Implementation pending |
| TX-013 | Retry behavior | 8.5 | Transaction slice | Retry test | alpha.4 | Pending | Not started | Implementation pending |
| TX-014 | UNKNOWN handling | 8.5 | Transaction slice | Failure/recovery test | alpha.4 | Pending | Not started | Implementation pending |
| TX-015 | Manual reconcile | 8.5 | Transaction/admin slice | Reconcile test | alpha.4 | Pending | Not started | Implementation pending |
| TX-016 | Automatic reconcile | 8.5 | Transaction slice | Restart/reconcile test | alpha.4 | Pending | Not started | Implementation pending |
| TX-017 | Provider reference | 8.5 | Transaction slice | Persistence test | alpha.4 | Pending | Not started | Implementation pending |
| TX-018 | Transaction audit | 8.5 | Transaction/audit slice | Audit test | alpha.4 | Pending | Not started | Implementation pending |
| TX-019 | Transaction restart recovery | 8.5 | Transaction slice | Restart test | alpha.4 | Pending | Not started | Implementation pending |
| TX-020 | Insufficient funds | 8.5 | Provider adapter | Provider fixture | alpha.4 | Pending | Not started | Implementation pending |
| TX-021 | Provider outage | 8.5 | Provider adapter | Outage test | alpha.4 | Pending | Not started | Implementation pending |
| TX-022 | Do not claim unconditional exactly-once | 8.5 | API/docs pending | Documentation test | beta.1 | N/A | Not started | Must state compensation limits |
| TX-023 | Do not access RedisEconomy internal keys | 8.5, 14 | Adapter boundary | Code inspection | alpha.4 | Pending | Not started | Implementation pending |
| TX-024 | Use supported API/Vault boundary | 8.5 | Adapter boundary | Capability/thread test | alpha.4 | Pending | Not started | Owner decision if unavailable |
| AUD-001 | Audit enable/disable/migration | 8.6 | Probe, enable/migration events, bounded close event | Lifecycle/persistence suite | alpha.2 | Pending | In progress | Implementation complete; MariaDB CI and Paper runtime pending |
| AUD-002 | Audit health transitions/dependency outage | 8.6 | Dynamic UP/DOWN/recovery/close health | Validation/failure/integration suite | alpha.2/3 | Pending | In progress | Caller failures stay UP; infrastructure paths implemented; CI pending |
| AUD-003 | Audit transaction/refund/UNKNOWN | 8.6 | Audit slice | Transaction test | alpha.4 | Pending | Not started | Implementation pending |
| AUD-004 | Audit reconcile/admin action | 8.6 | Audit/admin slice | Admin test | alpha.4 | Pending | Not started | Implementation pending |
| AUD-005 | Audit identity failures | 8.6 | Mandatory durable invalid-item and player-upsert-failure events | Unit/integration identity suite | alpha.2 | Pending | In progress | Identity failure attempts durable safe audit; audit failure remains exceptional; MariaDB CI pending |
| AUD-006 | Audit permission denial/shutdown timeout | 8.6 | Async durable permission sink plus bounded shutdown warning | Permission/timeout tests | alpha.2 | Pending | In progress | Permission persistence CI pending; shutdown-timeout durable record not guaranteed |
| AUD-007 | Redact secrets in audit | 8.6 | Configured server authority plus pre-DB all-string JSON/key/resolved-secret/credential/URI rejection | Unit and sentinel integration tests | beta.1 | Pending | In progress | Caller server mismatch and sensitive persistent strings fail before DB; MariaDB zero-hit CI pending |
| AUD-008 | Do not silently drop critical events | 8.6 | Exceptional completion and observed warning/health | Failure/backpressure tests | beta.1 | Pending | In progress | Failure semantics implemented; bounded queue/backpressure remains alpha.3 |
| ID-001 | Player UUID authority | 8.7 | UUID-keyed player repository and join snapshot | Repository/restart tests | alpha.2 | Pending | In progress | Implementation complete; MariaDB CI pending |
| ID-002 | Last-known name is auxiliary only | 8.7 | Non-unique indexed name; stale update ignored | Repository tests | alpha.2 | Pending | In progress | Schema and repository complete; MariaDB CI pending |
| ID-003 | Do not reimplement authentication/permission/inventory | 8.7 | V002 and API boundary contain identity metadata only | Boundary/schema inspection | beta.1 | Pending | In progress | Source boundary implemented; beta packaging acceptance pending |
| ID-004 | PDC `item_type` | 8.7 | Common constant and raw claim | Contract/negative tests | alpha.2 | Pending | Automated test passed | Paper adapter runtime pending |
| ID-005 | PDC `owner_uuid` | 8.7 | Common constant and raw claim | Contract/negative tests | alpha.2 | Pending | Automated test passed | Paper adapter runtime pending |
| ID-006 | PDC `instance_epoch` | 8.7 | Common constant and raw claim | Contract/negative tests | alpha.2 | Pending | Automated test passed | Paper adapter runtime pending |
| ID-007 | PDC `schema_version` | 8.7 | Common constant and raw claim | Contract/negative tests | alpha.2 | Pending | Automated test passed | Paper adapter runtime pending |
| ID-008 | PDC `item_instance_id` | 8.7 | Common constant and raw claim | Contract/negative tests | alpha.2 | Pending | Automated test passed | Paper adapter runtime pending |
| ID-009 | PDC `display_revision` | 8.7 | Common constant and raw claim | Contract/negative tests | alpha.2 | Pending | Automated test passed | Paper adapter runtime pending |
| ID-010 | Do not identify by lore/name/material alone | 8.7 | Six-claim JDK contract excludes presentation fields | API/source inspection | alpha.2 | Pending | Automated test passed | Main/Frontier adapters pending |
| ID-011 | Unknown schema/type fails closed | 8.7 | Ordered raw-claim validator | Negative unit/integration tests | alpha.2 | Pending | In progress | Unit passed; MariaDB CI pending |
| ID-012 | Invalid UUID fails closed | 8.7 | Canonical UUID parser | Negative unit tests | alpha.2 | Pending | Automated test passed | Runtime pending |
| ID-013 | Owner mismatch fails closed | 8.7 | Persisted/raw/expected owner comparison | Negative unit/integration tests | alpha.2 | Pending | In progress | Unit passed; MariaDB CI pending |
| ID-014 | Epoch mismatch fails closed | 8.7 | Persisted/raw/expected epoch comparison | Negative unit/integration tests | alpha.2 | Pending | In progress | Unit passed; MariaDB CI pending |
| RED-001 | Redis cache foundation | 8.8 | Redis slice | Integration test | alpha.3 | Pending | Not started | Implementation pending |
| RED-002 | Redis lock foundation | 8.8 | Redis slice | Concurrency test | alpha.3 | Pending | Not started | Implementation pending |
| RED-003 | Redis pub/sub and invalidation | 8.8 | Redis slice | Integration test | alpha.3 | Pending | Not started | Implementation pending |
| RED-004 | Redis message foundation | 8.8 | Redis slice | Integration test | alpha.3 | Pending | Not started | Implementation pending |
| RED-005 | Redis idempotency assistance | 8.8 | Redis slice | Idempotency test | alpha.3/4 | Pending | Not started | Implementation pending |
| RED-006 | MariaDB authority retained | 8.8 | Boundary | Outage/restart test | alpha.3 | Pending | Not started | Implementation pending |
| RED-007 | Outage degradation documented | 8.8 | Handoff pending | Documentation test | alpha.3 | Pending | Not started | Implementation pending |
| RED-008 | Outage rejection documented | 8.8 | Handoff pending | Documentation test | alpha.3 | Pending | Not started | Implementation pending |
| RED-009 | Redis reconnect | 8.8, 10.2 | Redis slice | Reconnect test | alpha.3 | Pending | Not started | Implementation pending |
| TASK-001 | Main-thread snapshot | 8.9 | Task slice | Thread contract test | alpha.3 | Pending | Not started | Implementation pending |
| TASK-002 | Immutable request | 8.9 | Task slice | Immutability test | alpha.3 | Pending | Not started | Implementation pending |
| TASK-003 | Async operation | 8.9 | Task slice | Thread test | alpha.3 | Pending | Not started | Implementation pending |
| TASK-004 | Immutable result | 8.9 | Task slice | Immutability test | alpha.3 | Pending | Not started | Implementation pending |
| TASK-005 | Main-thread revalidation | 8.9 | Task slice | Stale-result test | alpha.3 | Pending | Not started | Implementation pending |
| TASK-006 | Bukkit mutation only on main thread | 6.4, 8.9 | Task slice | Thread test | alpha.3 | Pending | Not started | Implementation pending |
| TASK-007 | No mutable Bukkit object retained async | 8.9 | Task slice | Leak/thread test | alpha.3 | Pending | Not started | Implementation pending |
| TASK-008 | Bounded executor queue | 8.9 | Task slice | Queue test | alpha.3 | Pending | Not started | Implementation pending |
| TASK-009 | Backpressure | 8.9 | Task slice | Pressure test | alpha.3 | Pending | Not started | Implementation pending |
| TASK-010 | Graceful shutdown | 8.9 | Persistence work gate and `ManagedExecutor`; broader task slice pending | Queued accepted-work drain and shutdown tests | alpha.2/3 | Pending | In progress | Persistence work drains before graceful executor termination; bounded queue/backpressure remains alpha.3 |
| TASK-011 | No main-thread DB I/O | 6.4, 8.9 | Internal database executor, persistence gate, and `ThreadContext` guard | Pre-acquisition guard and stopping-race tests | alpha.2/3 | Pending | Automated test passed | Worker avoids lifecycle synchronization; guarded connection acquisition remained zero; Paper runtime pending |
| TASK-012 | No main-thread Redis I/O | 6.4, 8.9 | Task/Redis slice | Detection test | alpha.3 | Pending | Not started | Implementation pending |
| TASK-013 | Reject callback after disable | 8.9 | Task slice | Race test | alpha.3 | Pending | Not started | Implementation pending |
| ADM-001 | Health: Config/MariaDB/Migration | 8.10 | Active persistence lifecycle health | Success/failure/disable integration tests | V0.0.1-alpha.1 / alpha.2 | alpha.1 runtime evidence | In progress | Automated `UNKNOWN`/`UP`/`DOWN`/`DISABLED` paths passed; alpha.2 runtime pending |
| ADM-002 | Health: Redis/Waymark | 8.10 | Health slice | Health test | alpha.3/4 | Pending | Not started | Implementation pending |
| ADM-003 | Health: Audit/Transaction | 8.10 | Dynamic Audit and Identity components | Health/failure tests | alpha.2/4 | Pending | In progress | Audit/Identity implemented; Transaction remains alpha.4 |
| ADM-004 | Health: Executor/Services | 8.10 | Dynamic executor/services components | Health/runtime tests | V0.0.1-alpha.1 / alpha.3 | alpha.1 runtime evidence | In progress | Executor and Services `UP`; later executor slice remains pending |
| ADM-005 | `/wayfarer admin health` | 8.10 | `HealthCommandHandler`; Bukkit adapter | Command suite | V0.0.1-alpha.1 | alpha.1 runtime evidence | Runtime test passed | Console and authorized-player output observed |
| ADM-006 | Transaction inspect command | 8.10 | Admin slice | Command test | alpha.4 | Pending | Not started | Implementation pending |
| ADM-007 | Transaction reconcile command | 8.10 | Admin slice | Command test | alpha.4 | Pending | Not started | Implementation pending |
| ADM-008 | Admin permission rules | 8.10 | `wayfarer.admin.health` | Authorization/denial tests | V0.0.1-alpha.1 / alpha.4 | alpha.1 runtime evidence | In progress | Non-OP denial and OP authorization observed; later admin grants pending |
| ADM-009 | Console eligibility | 8.10 | Bukkit audience adapter | Console command tests | V0.0.1-alpha.1 / alpha.4 | alpha.1 runtime evidence | In progress | Console health observed; later admin command slices pending |
| ADM-010 | Admin redaction/audit/confirmation | 8.10 | Admin slice | Security test | alpha.4 | Pending | Not started | Implementation pending |
| TST-001 | Unit tests | 10.1 | API/Common/Core test sources | 120 PR B unit tests | V0.0.1-alpha.1+ | N/A | In progress | Local: 120 passed, 0 failed, 0 skipped; final CI pending |
| TST-002 | MariaDB integration | 10.1 | Testkit fixture; mandatory Core integration task | Persistence plus PR B Testcontainers suite | alpha.2 | Pending | In progress | Local Docker stopped; source compiled; GitHub Actions mandatory authority |
| TST-003 | Redis integration | 10.1 | alpha.3 plan | Testcontainers | alpha.3 | Pending | Not started | Implementation pending |
| TST-004 | Migration tests | 10.1 | Alpha.2 persistence/audit/identity suite | Empty/V001-upgrade/repeated/failure/hash tests | alpha.2 | Pending | In progress | Existing V001 tests passed; V002 GitHub Actions execution pending |
| TST-005 | Concurrency tests | 10.1 | Persistence drain and newer-only identity semantics | Queue/drain/stale observation tests | alpha.2+ | Pending | In progress | Existing drain passed; identity MariaDB CI pending; broader concurrency later |
| TST-006 | Idempotency tests | 10.1 | alpha.4 plan | Automated suite | alpha.4 | Pending | Not started | Implementation pending |
| TST-007 | Failure/timeout tests | 10.1 | Lifecycle/executor and persistence-drain failure suites | Gradle test | V0.0.1-alpha.1+ | alpha.1 runtime evidence | In progress | Drain timeout/interruption are bounded, warned, and non-clean; Paper persistence evidence pending |
| TST-008 | Restart recovery tests | 10.1 | Audit/player/item repository reconstruction | MariaDB restart suite | alpha.2+ | Pending | In progress | Source compiled; GitHub Actions execution pending |
| TST-009 | Config validation tests | 10.1 | `CoreConfigLoaderTest` | Gradle test | V0.0.1-alpha.1 | alpha.1 runtime evidence | Runtime test passed | Placeholder, valid, unsupported, and dependency-validation paths observed |
| TST-010 | Secret redaction tests | 10.1 | Common/config/health/audit sanitizer | Unit plus zero-hit MariaDB test | beta.1 | alpha.1 runtime evidence | In progress | PR B unit passed; MariaDB CI and full beta scan pending |
| TST-011 | Packaging/API compatibility tests | 10.1 | beta plan | Automated inspection | V0.0.1-alpha.1 / beta.1 | alpha.1 runtime evidence | In progress | Alpha.1 packaging and class isolation passed; full beta suite pending |
| TST-012 | Isolated test server | 10.2 | Result and evidence index | Runtime procedure | V0.0.1-alpha.1–rc.1 | alpha.1 runtime evidence | In progress | Alpha.1 runtime gate passed; later candidates and Project acceptance pending |
| TST-013 | Main-thread I/O and callback tests | 10.2 | Persistence guard plus immutable player join snapshot | Automated/runtime | alpha.2/3 | Pending | In progress | JDBC guard passed; listener stores no Player async; Paper and Redis paths pending |
| TST-014 | Tick/performance verification | 10.2, 11 | RC plan | Runtime measurement | rc.1 | Pending | Not started | Implementation pending |
| TST-015 | Permission denial verification | 10.2 | Async operational audit sink with actor snapshot | Unit/MariaDB permission tests | V0.0.1-alpha.1 / alpha.4 | alpha.1 runtime evidence | In progress | Unit command path passed; durable MariaDB CI pending; later admin slices pending |
| TST-016 | JAR dependency/class identity inspection | 10.2 | beta plan | Packaging test | V0.0.1-alpha.1 / beta.1 | alpha.1 runtime evidence | In progress | Alpha.1 Core JAR and Probe isolation passed; full beta inspection pending |
| TST-017 | Test Report release/artifact/environment identity | 11 | Report skeleton | Document validation | rc.1 | Pending | Not started | Final values pending |
| TST-018 | Test Report authority/scope/policy compliance | 11 | Report skeleton | Document validation | rc.1 | Pending | Not started | Final evidence pending |
| TST-019 | Test Report build/unit results and counts | 11 | Report skeleton | Document validation | rc.1 | Pending | Not started | No test source yet |
| TST-020 | Test Report MariaDB/Redis/migration results | 11 | Report skeleton | Document validation | rc.1 | Pending | Not started | Tests pending |
| TST-021 | Test Report isolated server cases expected/actual/evidence | 11 | Report skeleton | Document validation | rc.1 | Pending | Not started | Runtime pending |
| TST-022 | Test Report threading/failure/restart/performance | 11 | Report skeleton | Document validation | rc.1 | Pending | Not started | Runtime pending |
| TST-023 | Test Report API/packaging/permission/redaction | 11 | Report skeleton | Document validation | rc.1 | Pending | Not started | Tests pending |
| TST-024 | Test Report limitations/failures/open decisions | 11 | Report skeleton | Document validation | rc.1 | Pending | Not started | Final assessment pending |
| TST-025 | Test Report reproduction/evidence/runtime-non-change | 11 | Report skeleton | Document validation | rc.1 | Pending | Not started | Final evidence pending |
| REL-001 | Human versions/tags/JAR names use uppercase V | 12, 15 | Workflows/process | Workflow tests | All | V0.0.1-alpha.1 | Implemented | Alpha.1 tag and release artifact naming verified |
| REL-002 | Only V0.0.x versions accepted | 12, 15 | Workflow regex | Negative-path tests | All | N/A | Implemented | Dispatch not executed |
| REL-003 | Gradle/plugin version strips leading V | 12 | Workflows | Workflow assertion | All | V0.0.1-alpha.1 | Implemented | Runtime plugin reported `0.0.1-alpha.1` |
| REL-004 | `release_scope=core` required | 3, 12 | Workflows | Negative-path tests | All | N/A | Implemented | Other scopes fail closed |
| REL-005 | Pre-release/stable scope match | 12 | Stable workflow | Negative-path test | Stable | N/A | Implemented | Dispatch not executed |
| REL-006 | Environment approval retained | 12, 15 | Workflows | YAML assertion | All | V0.0.1-alpha.1 | Implemented | Alpha.1 pre-release environment approval was exercised |
| REL-007 | Explicit `requirements_cleared=true` | 12, 15 | Stable workflow | Negative-path test | Stable | N/A | Implemented | Owner-controlled |
| REL-008 | Traceability gate `CLEARED` required | 12, 13 | Stable workflow | Negative-path test | Stable | N/A | Implemented | Current marker BLOCKED |
| REL-009 | Release readiness `READY` required | 12, 13 | Stable workflow | Negative-path test | Stable | N/A | Implemented | Current marker BLOCKED |
| REL-010 | Traceability/readiness are tracked fixed inputs | 12, 13 | Stable workflow | Path/tracking tests | Stable | N/A | Implemented | Release not executed |
| REL-011 | Traceability/readiness copied, hashed, manifested | 12, 13 | Stable workflow | Packaging test | Stable | N/A | Implemented | Release not executed |
| REL-012 | Runtime JAR and SHA-256 | 12 | Workflow | Packaging test | V0.0.1-alpha.1 / beta.1+ | alpha.1 runtime evidence | In progress | Alpha.1 release JAR identity verified; later candidates pending |
| REL-013 | Release notes/config/migration metadata | 12 | Workflow/handoff | Packaging test | beta.1+ | N/A | Not started | Final content pending |
| REL-014 | Sanitized config asset | 12 | Handoff skeleton | Packaging test | beta.1+ | N/A | Not started | Final asset pending |
| REL-015 | Command/permission reference asset | 12 | Handoff skeleton | Packaging test | beta.1+ | N/A | Not started | Final asset pending |
| REL-016 | Dependency list asset | 12 | Handoff skeleton | Packaging test | beta.1+ | N/A | Not started | Final asset pending |
| REL-017 | Bundled/relocated library list asset | 12 | Pending | Packaging test | beta.1+ | N/A | Not started | Final asset pending |
| REL-018 | License/third-party notice assets | 12 | License; notice pending | Packaging test | beta.1+ | N/A | Not started | Notice pending |
| REL-019 | Plugin Test Report asset | 12 | Report skeleton | Packaging test | rc.1 | N/A | Not started | Final report pending |
| REL-020 | Known limitations asset | 12 | Handoff skeleton | Packaging test | rc.1 | N/A | Not started | Final content pending |
| REL-021 | Rollback/removal asset | 12 | Handoff skeleton | Packaging test | rc.1 | N/A | Not started | Final content pending |
| REL-022 | Artifact Matrix asset | 13.2 | Handoff skeleton | Packaging test | rc.1 | N/A | Not started | Final content pending |
| REL-023 | Project acceptance input asset | 13.3 | Handoff skeleton | Packaging test | rc.1 | N/A | Not started | Final content pending |
| REL-024 | Release process performs no Project Runtime change | 6.5, 14 | Workflow/process | Boundary inspection | All | N/A | Implemented | No deployment action |
| HND-001 | Release URL/tag/version/source commit | 13.1 | Handoff skeleton | Document validation | rc.1 | N/A | Not started | Values pending |
| HND-002 | Artifact/JAR/hash/config/migration inventory | 13.1 | Handoff skeleton | Document validation | rc.1 | N/A | Not started | Values pending |
| HND-003 | Sanitized config and environment variables | 13.1, 13.3 | Handoff skeleton | Document validation | rc.1 | Pending | Not started | Final content pending |
| HND-004 | Commands and permissions | 13.1 | Handoff skeleton | Document validation | rc.1 | Pending | Not started | Final content pending |
| HND-005 | Dependencies/placement/load order | 13.1, 13.3 | Handoff skeleton | Document validation | rc.1 | Pending | Not started | Final content pending |
| HND-006 | Test report/limitations/open decisions | 13.1 | Handoff skeleton | Document validation | rc.1 | Pending | Not started | Final content pending |
| HND-007 | Upgrade/rollback/removal/data compatibility | 13.1, 13.3 | Handoff skeleton | Document validation | rc.1 | Pending | Not started | Final content pending |
| HND-008 | Artifact Matrix | 13.2 | `artifact-matrix.md` | Document validation | rc.1 | N/A | In progress | Skeleton only |
| HND-009 | Runtime/DB/Redis/external-plugin prerequisites | 13.3 | Handoff skeleton | Document validation | rc.1 | Pending | Not started | Final content pending |
| HND-010 | Migration/failure/health/smoke-test behavior | 13.3 | Handoff skeleton | Document validation | rc.1 | Pending | Not started | Final content pending |
| HND-011 | Backup/restore/removal/rollback/downgrade | 13.3 | Handoff skeleton | Document validation | rc.1 | Pending | Not started | Final content pending |
| HND-012 | Test-server differences and Project acceptance input | 13.3 | Handoff skeleton | Document validation | rc.1 | Pending | Not started | Final content pending |
| HND-013 | Project acceptance and Roadmap remain pending | 9.3, 13 | Handoff skeleton | Document validation | Stable | N/A | Implemented | Runtime not changed |
| OPS-001 | No force push/tag or asset overwrite | 15 | Workflow/policy | Git/release inspection | All | N/A | Implemented | Recheck every publication |
| OPS-002 | Commit/Test Report/release provenance | 15 | Workflow/handoff | Provenance test | rc.1 | N/A | Not started | Release pending |
| OPS-003 | Failed/skipped tests are disclosed | 11, 15 | Result/report skeleton | Document validation | All | N/A | In progress | No test source disclosed |
| OPS-004 | Generated artifacts remain out of source history | 15 | `.gitignore`; policy | Git inspection | All | N/A | Implemented | No binary tracked |
