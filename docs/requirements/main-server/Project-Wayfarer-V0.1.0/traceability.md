# Requirement Traceability

- Release gate: BLOCKED
- Acceptance units: 245

The Project mainline requirement snapshot is the authority for source sections. A documentation
skeleton does not satisfy the underlying implementation or test requirement. `Passed` and
`Runtime test passed` require commit-pinned implementation, automated results, and runtime
evidence as applicable.

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
| FND-001 | Java 25 baseline | 7 | Gradle toolchain | Build validation | beta.1 | N/A | In progress | Build passes; clean evidence pending |
| FND-002 | Gradle 9.6.1 baseline | 7 | Wrapper properties | Wrapper validation | beta.1 | N/A | In progress | Checksum evidence pending |
| FND-003 | Kotlin DSL build | 7 | `*.gradle.kts` | Build validation | beta.1 | N/A | In progress | Full acceptance pending |
| FND-004 | Paper API 1.21.11 baseline | 7 | Version catalog | Dependency inspection | beta.1 | Pending | In progress | Runtime validation pending |
| FND-005 | Group/package `io.github.eariver.wayfarer` | 7 | Build/source | Package inspection | beta.1 | N/A | In progress | Full artifact inspection pending |
| FND-006 | Gradle Wrapper tracked | 7 | `gradlew*`; `gradle/wrapper` | Git inspection | beta.1 | N/A | In progress | Clean clone test pending |
| FND-007 | Wrapper version verified | 7 | Wrapper properties | Wrapper test | beta.1 | N/A | Not started | Evidence pending |
| FND-008 | Wrapper checksum verified | 7 | Pending verification metadata | Checksum test | beta.1 | N/A | Not started | Evidence pending |
| FND-009 | Clean `check` succeeds | 7 | Gradle | Clean build | beta.1 | N/A | Not started | Current cached build is not clean evidence |
| FND-010 | Clean `assemble` succeeds | 7 | Gradle | Clean build | beta.1 | N/A | Not started | Current cached build is not clean evidence |
| FND-011 | CI uses Java 25 | 7 | `ci.yml` | CI run | beta.1 | N/A | In progress | PR CI evidence pending correction |
| FND-012 | Configuration cache compatible | 7 | Gradle configuration | Cache check/assemble | beta.1 | N/A | Not started | Correction validation pending |
| FND-013 | Versions centralized | 7 | Version catalog/properties | Dependency inspection | beta.1 | N/A | In progress | Full inspection pending |
| FND-014 | Compiler warnings fail build | 7 | `-Xlint:all -Werror` | Compile test | beta.1 | N/A | In progress | Source coverage incomplete |
| FND-015 | Reproducible artifact | 7 | Build configuration pending | Rebuild/hash test | beta.1 | N/A | Not started | Implementation pending |
| FND-016 | Source commit maps to artifact hash | 7, 12 | Release manifest | Packaging test | beta.1 | N/A | Not started | No release artifact |
| FND-017 | API class identity preserved | 7, 10.1 | Packaging design pending | Class identity test | beta.1 | Pending | Not started | Implementation pending |
| FND-018 | Runtime JAR excludes tests/secrets/runtime data | 7 | Packaging design pending | JAR inspection | beta.1 | N/A | Not started | Implementation pending |
| FND-019 | Generated JAR is not tracked | 7, 15 | `.gitignore`; policy | Git inspection | All | N/A | Implemented | No JAR tracked |
| FND-020 | License supplied | 7, 12 | `LICENSE` | Package inspection | beta.1 | N/A | In progress | Release inclusion pending |
| FND-021 | Third-party notice supplied | 7, 12 | Pending | Package inspection | beta.1 | N/A | Not started | Document pending |
| FND-022 | Secrets/runtime/world/DB/log/cache not tracked | 7, 14 | `.gitignore`; policy | Git inspection | All | N/A | Implemented | Recheck each release |
| LIF-001 | Typed config validation | 8.1 | Lifecycle slice | Unit/config test | alpha.1 | Pending | Not started | Implementation pending |
| LIF-002 | Environment secret resolution | 8.1 | Lifecycle slice | Missing/redaction test | alpha.1 | Pending | Not started | Implementation pending |
| LIF-003 | MariaDB pool lifecycle | 8.1 | Persistence slice | Integration test | alpha.2 | Pending | Not started | Implementation pending |
| LIF-004 | Flyway validate | 8.1 | Persistence slice | Migration test | alpha.2 | Pending | Not started | Implementation pending |
| LIF-005 | Flyway migrate | 8.1 | Persistence slice | Migration test | alpha.2 | Pending | Not started | Implementation pending |
| LIF-006 | Redis foundation | 8.1 | Redis slice | Integration test | alpha.3 | Pending | Not started | Implementation pending |
| LIF-007 | Redis health | 8.1 | Redis slice | Outage/reconnect test | alpha.3 | Pending | Not started | Implementation pending |
| LIF-008 | Waymark capability probe | 8.1 | Transaction slice | Provider fixture | alpha.4 | Pending | Not started | Provider contract gate |
| LIF-009 | Services registration | 8.1 | Lifecycle slice | Lifecycle test | alpha.1 | Pending | Not started | Implementation pending |
| LIF-010 | Services unregister | 8.1 | Lifecycle slice | Disable test | alpha.1 | Pending | Not started | Implementation pending |
| LIF-011 | Executor lifecycle | 8.1 | Lifecycle/task slice | Shutdown test | alpha.1/3 | Pending | Not started | Implementation pending |
| LIF-012 | Scheduler lifecycle | 8.1 | Lifecycle slice | Shutdown test | alpha.1 | Pending | Not started | Implementation pending |
| LIF-013 | Clean disable | 8.1 | Lifecycle slice | Disable/restart test | alpha.1 | Pending | Not started | Implementation pending |
| LIF-014 | Timeout-bounded flush | 8.1 | Lifecycle slice | Timeout test | alpha.1 | Pending | Not started | Implementation pending |
| LIF-015 | Partial initialization cleanup | 8.1 | Lifecycle slice | Failure injection | alpha.1 | Pending | Not started | Implementation pending |
| LIF-016 | Fail-closed lifecycle | 8.1 | Lifecycle slice | Negative-path test | alpha.1 | Pending | Not started | Implementation pending |
| LIF-017 | Reject callbacks after disable | 8.1 | Task slice | Race test | alpha.1/3 | Pending | Not started | Implementation pending |
| LIF-018 | Do not continue after migration failure | 8.1 | Persistence slice | Migration failure test | alpha.2 | Pending | Not started | Implementation pending |
| API-001 | `WayfarerServices` contract | 8.2 | `wayfarer-api` | API compatibility test | beta.1 | Pending | Not started | Existing stub not accepted |
| API-002 | `WayfarerDatabase` contract | 8.2 | `wayfarer-api` | API compatibility test | beta.1 | Pending | Not started | Existing stub not accepted |
| API-003 | `WayfarerAudit` contract | 8.2 | `wayfarer-api` | API compatibility test | beta.1 | Pending | Not started | Existing stub not accepted |
| API-004 | `WayfarerTransactions` contract | 8.2 | `wayfarer-api` | API compatibility test | beta.1 | Pending | Not started | Existing stub not accepted |
| API-005 | `WayfarerWaymark` contract | 8.2 | `wayfarer-api` | API compatibility test | beta.1 | Pending | Not started | Existing stub not accepted |
| API-006 | `WayfarerItemIdentity` contract | 8.2 | `wayfarer-api` | API compatibility test | beta.1 | Pending | Not started | Existing stub not accepted |
| API-007 | `WayfarerTasks` contract | 8.2 | `wayfarer-api` | API compatibility test | beta.1 | Pending | Not started | Existing stub not accepted |
| API-008 | `WayfarerHealth` contract | 8.2 | `wayfarer-api` | API compatibility test | beta.1 | Pending | Not started | Existing stub not accepted |
| API-009 | Service lookup/thread/completion/timeout semantics documented | 8.2 | API design pending | Contract tests | beta.1 | Pending | Not started | Implementation pending |
| API-010 | Idempotency/disable/health semantics documented | 8.2 | API design pending | Contract tests | beta.1 | Pending | Not started | Implementation pending |
| API-011 | Item/transaction/Waymark/audit/task semantics documented | 8.2 | API design pending | Contract tests | beta.1 | Pending | Not started | Implementation pending |
| API-012 | Hikari not exposed | 8.2 | API boundary | Leak test | beta.1 | N/A | Not started | Inspection pending |
| API-013 | Flyway not exposed | 8.2 | API boundary | Leak test | beta.1 | N/A | Not started | Inspection pending |
| API-014 | Lettuce not exposed | 8.2 | API boundary | Leak test | beta.1 | N/A | Not started | Inspection pending |
| API-015 | JDBC Connection/plugin implementation not exposed | 8.2 | API boundary | Leak test | beta.1 | N/A | Not started | Inspection pending |
| API-016 | Paper Event/Player/ItemStack not exposed | 8.2 | API boundary | Leak test | beta.1 | N/A | Not started | Inspection pending |
| API-017 | Server gameplay domain not exposed | 8.2 | API boundary | Leak test | beta.1 | N/A | Not started | Inspection pending |
| CFG-001 | Config version | 8.3 | Lifecycle slice | Config test | alpha.1 | Pending | Not started | Required before release |
| CFG-002 | Server ID | 8.3 | Lifecycle slice | Config test | alpha.1 | Pending | Not started | Implementation pending |
| CFG-003 | MariaDB connection settings | 8.3 | Persistence slice | Config/redaction test | alpha.2 | Pending | Not started | Implementation pending |
| CFG-004 | Redis connection settings | 8.3 | Redis slice | Config/redaction test | alpha.3 | Pending | Not started | Implementation pending |
| CFG-005 | Migration settings | 8.3 | Persistence slice | Config test | alpha.2 | Pending | Not started | Implementation pending |
| CFG-006 | Waymark provider settings | 8.3 | Transaction slice | Config test | alpha.4 | Pending | Not started | Implementation pending |
| CFG-007 | Executor settings | 8.3 | Lifecycle/task slice | Validation test | alpha.1/3 | Pending | Not started | Implementation pending |
| CFG-008 | Audit settings | 8.3 | Persistence slice | Validation test | alpha.2 | Pending | Not started | Implementation pending |
| CFG-009 | Health settings | 8.3 | Lifecycle slice | Validation test | alpha.1 | Pending | Not started | Implementation pending |
| CFG-010 | Shutdown timeout settings | 8.3 | Lifecycle slice | Timeout test | alpha.1 | Pending | Not started | Implementation pending |
| CFG-011 | Sanitized sample | 8.3, 12 | Handoff skeleton | Secret scan | beta.1 | N/A | Not started | Skeleton is not final sample |
| CFG-012 | No secret in Git/YAML | 8.3 | Policy | Secret scan | beta.1 | N/A | Not started | Full release scan pending |
| CFG-013 | No secret in logs | 8.3 | Redaction pending | Runtime redaction test | beta.1 | Pending | Not started | Implementation pending |
| CFG-014 | No secret in audit | 8.3 | Redaction pending | Audit redaction test | beta.1 | Pending | Not started | Implementation pending |
| CFG-015 | No secret in exceptions | 8.3 | Redaction pending | Failure test | beta.1 | Pending | Not started | Implementation pending |
| DB-001 | Core owns only `wf_core_*` | 8.4 | Core migration | Schema inspection | alpha.2 | Pending | Not started | Implementation pending |
| DB-002 | Transaction domain | 8.4 | Core migration/repository | Integration test | alpha.2/4 | Pending | Not started | Implementation pending |
| DB-003 | Transaction event/history domain | 8.4 | Core migration/repository | Integration test | alpha.2/4 | Pending | Not started | Implementation pending |
| DB-004 | Audit domain | 8.4 | Core migration/repository | Integration test | alpha.2 | Pending | Not started | Implementation pending |
| DB-005 | Player identity domain | 8.4 | Core migration/repository | Integration test | alpha.2 | Pending | Not started | Implementation pending |
| DB-006 | Item identity domain | 8.4 | Core migration/repository | Integration test | alpha.2 | Pending | Not started | Implementation pending |
| DB-007 | Reconcile state domain | 8.4 | Core migration/repository | Recovery test | alpha.2/4 | Pending | Not started | Implementation pending |
| DB-008 | Empty DB migration | 8.4 | Flyway slice | Testcontainers | alpha.2 | Pending | Not started | Implementation pending |
| DB-009 | Additive migration | 8.4 | Flyway policy | Migration test | alpha.2 | Pending | Not started | Implementation pending |
| DB-010 | Applied migration immutable | 8.4 | Policy | Checksum test | alpha.2+ | N/A | In progress | Existing migration unchanged |
| DB-011 | UUID authority | 8.4 | Persistence slice | Repository test | alpha.2 | Pending | Not started | Implementation pending |
| DB-012 | UTC timestamps | 8.4 | Migration/repository | DB test | alpha.2 | Pending | Not started | Implementation pending |
| DB-013 | Unique constraints | 8.4 | Migration | DB test | alpha.2 | Pending | Not started | Implementation pending |
| DB-014 | Optimistic locking | 8.4 | Repository | Concurrency test | alpha.2 | Pending | Not started | Implementation pending |
| DB-015 | Required indexes | 8.4 | Migration | Schema test | alpha.2 | Pending | Not started | Implementation pending |
| DB-016 | JSON validation | 8.4 | Migration/repository | DB test | alpha.2 | Pending | Not started | Implementation pending |
| DB-017 | Restart recovery | 8.4 | Persistence slice | Restart test | alpha.2 | Pending | Not started | Implementation pending |
| DB-018 | Core does not create `wf_main_*` | 8.4 | Migration boundary | Schema test | alpha.2 | Pending | Not started | Implementation pending |
| DB-019 | Core does not create `wf_frontier_*` | 8.4 | Migration boundary | Schema test | alpha.2 | Pending | Not started | Implementation pending |
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
| AUD-001 | Audit enable/disable/migration | 8.6 | Audit slice | Persistence test | alpha.2 | Pending | Not started | Implementation pending |
| AUD-002 | Audit health transitions/dependency outage | 8.6 | Audit slice | Failure test | alpha.2/3 | Pending | Not started | Implementation pending |
| AUD-003 | Audit transaction/refund/UNKNOWN | 8.6 | Audit slice | Transaction test | alpha.4 | Pending | Not started | Implementation pending |
| AUD-004 | Audit reconcile/admin action | 8.6 | Audit/admin slice | Admin test | alpha.4 | Pending | Not started | Implementation pending |
| AUD-005 | Audit identity failures | 8.6 | Audit/identity slice | Identity test | alpha.2 | Pending | Not started | Implementation pending |
| AUD-006 | Audit permission denial/shutdown timeout | 8.6 | Audit/lifecycle slice | Permission/timeout test | alpha.1 | Pending | Not started | Implementation pending |
| AUD-007 | Redact secrets in audit | 8.6 | Redaction pending | Redaction test | beta.1 | Pending | Not started | Implementation pending |
| AUD-008 | Do not silently drop critical events | 8.6 | Audit durability pending | Backpressure/failure test | beta.1 | Pending | Not started | Implementation pending |
| ID-001 | Player UUID authority | 8.7 | Identity slice | Identity test | alpha.2 | Pending | Not started | Implementation pending |
| ID-002 | Last-known name is auxiliary only | 8.7 | Identity slice | Repository test | alpha.2 | Pending | Not started | Implementation pending |
| ID-003 | Do not reimplement authentication/permission/inventory | 8.7 | Boundary | Code/schema inspection | beta.1 | Pending | Not started | Implementation pending |
| ID-004 | PDC `item_type` | 8.7 | Identity slice | PDC test | alpha.2 | Pending | Not started | Implementation pending |
| ID-005 | PDC `owner_uuid` | 8.7 | Identity slice | PDC test | alpha.2 | Pending | Not started | Implementation pending |
| ID-006 | PDC `instance_epoch` | 8.7 | Identity slice | PDC test | alpha.2 | Pending | Not started | Implementation pending |
| ID-007 | PDC `schema_version` | 8.7 | Identity slice | PDC test | alpha.2 | Pending | Not started | Implementation pending |
| ID-008 | PDC `item_instance_id` | 8.7 | Identity slice | PDC test | alpha.2 | Pending | Not started | Implementation pending |
| ID-009 | PDC `display_revision` | 8.7 | Identity slice | PDC test | alpha.2 | Pending | Not started | Implementation pending |
| ID-010 | Do not identify by lore/name/material alone | 8.7 | Identity slice | Forgery test | alpha.2 | Pending | Not started | Implementation pending |
| ID-011 | Unknown schema/type fails closed | 8.7 | Identity slice | Negative test | alpha.2 | Pending | Not started | Implementation pending |
| ID-012 | Invalid UUID fails closed | 8.7 | Identity slice | Negative test | alpha.2 | Pending | Not started | Implementation pending |
| ID-013 | Owner mismatch fails closed | 8.7 | Identity slice | Negative test | alpha.2 | Pending | Not started | Implementation pending |
| ID-014 | Epoch mismatch fails closed | 8.7 | Identity slice | Negative test | alpha.2 | Pending | Not started | Implementation pending |
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
| TASK-010 | Graceful shutdown | 8.9 | Task slice | Shutdown test | alpha.3 | Pending | Not started | Implementation pending |
| TASK-011 | No main-thread DB I/O | 6.4, 8.9 | Task/persistence slice | Detection test | alpha.2/3 | Pending | Not started | Implementation pending |
| TASK-012 | No main-thread Redis I/O | 6.4, 8.9 | Task/Redis slice | Detection test | alpha.3 | Pending | Not started | Implementation pending |
| TASK-013 | Reject callback after disable | 8.9 | Task slice | Race test | alpha.3 | Pending | Not started | Implementation pending |
| ADM-001 | Health: Config/MariaDB/Migration | 8.10 | Health slice | Health test | alpha.1/2 | Pending | Not started | Implementation pending |
| ADM-002 | Health: Redis/Waymark | 8.10 | Health slice | Health test | alpha.3/4 | Pending | Not started | Implementation pending |
| ADM-003 | Health: Audit/Transaction | 8.10 | Health slice | Health test | alpha.2/4 | Pending | Not started | Implementation pending |
| ADM-004 | Health: Executor/Services | 8.10 | Health slice | Health test | alpha.1/3 | Pending | Not started | Implementation pending |
| ADM-005 | `/wayfarer admin health` | 8.10 | Admin slice | Command test | alpha.1 | Pending | Not started | Implementation pending |
| ADM-006 | Transaction inspect command | 8.10 | Admin slice | Command test | alpha.4 | Pending | Not started | Implementation pending |
| ADM-007 | Transaction reconcile command | 8.10 | Admin slice | Command test | alpha.4 | Pending | Not started | Implementation pending |
| ADM-008 | Admin permission rules | 8.10 | Admin slice | Permission test | alpha.1/4 | Pending | Not started | Implementation pending |
| ADM-009 | Console eligibility | 8.10 | Admin docs pending | Command test | alpha.1/4 | Pending | Not started | Implementation pending |
| ADM-010 | Admin redaction/audit/confirmation | 8.10 | Admin slice | Security test | alpha.4 | Pending | Not started | Implementation pending |
| TST-001 | Unit tests | 10.1 | Phase plans | Gradle test | alpha.1+ | Pending | Not started | No test source yet |
| TST-002 | MariaDB integration | 10.1 | alpha.2 plan | Testcontainers | alpha.2 | Pending | Not started | Implementation pending |
| TST-003 | Redis integration | 10.1 | alpha.3 plan | Testcontainers | alpha.3 | Pending | Not started | Implementation pending |
| TST-004 | Migration tests | 10.1 | alpha.2 plan | Testcontainers | alpha.2 | Pending | Not started | Implementation pending |
| TST-005 | Concurrency tests | 10.1 | phase plans | Automated suite | alpha.2+ | Pending | Not started | Implementation pending |
| TST-006 | Idempotency tests | 10.1 | alpha.4 plan | Automated suite | alpha.4 | Pending | Not started | Implementation pending |
| TST-007 | Failure/timeout tests | 10.1 | phase plans | Automated suite | alpha.1+ | Pending | Not started | Implementation pending |
| TST-008 | Restart recovery tests | 10.1 | phase plans | Automated suite | alpha.2+ | Pending | Not started | Implementation pending |
| TST-009 | Config validation tests | 10.1 | alpha.1 plan | Automated suite | alpha.1 | Pending | Not started | Implementation pending |
| TST-010 | Secret redaction tests | 10.1 | phase plans | Automated suite | beta.1 | Pending | Not started | Implementation pending |
| TST-011 | Packaging/API compatibility tests | 10.1 | beta plan | Automated inspection | beta.1 | Pending | Not started | Implementation pending |
| TST-012 | Isolated test server | 10.2 | Result skeletons | Runtime procedure | alpha.1–rc.1 | Pending | Not started | User observation pending |
| TST-013 | Main-thread I/O and callback tests | 10.2 | phase plans | Automated/runtime | alpha.2/3 | Pending | Not started | Implementation pending |
| TST-014 | Tick/performance verification | 10.2, 11 | RC plan | Runtime measurement | rc.1 | Pending | Not started | Implementation pending |
| TST-015 | Permission denial verification | 10.2 | Phase plans | Command test | alpha.1/4 | Pending | Not started | Implementation pending |
| TST-016 | JAR dependency/class identity inspection | 10.2 | beta plan | Packaging test | beta.1 | Pending | Not started | Implementation pending |
| TST-017 | Test Report release/artifact/environment identity | 11 | Report skeleton | Document validation | rc.1 | Pending | Not started | Final values pending |
| TST-018 | Test Report authority/scope/policy compliance | 11 | Report skeleton | Document validation | rc.1 | Pending | Not started | Final evidence pending |
| TST-019 | Test Report build/unit results and counts | 11 | Report skeleton | Document validation | rc.1 | Pending | Not started | No test source yet |
| TST-020 | Test Report MariaDB/Redis/migration results | 11 | Report skeleton | Document validation | rc.1 | Pending | Not started | Tests pending |
| TST-021 | Test Report isolated server cases expected/actual/evidence | 11 | Report skeleton | Document validation | rc.1 | Pending | Not started | Runtime pending |
| TST-022 | Test Report threading/failure/restart/performance | 11 | Report skeleton | Document validation | rc.1 | Pending | Not started | Runtime pending |
| TST-023 | Test Report API/packaging/permission/redaction | 11 | Report skeleton | Document validation | rc.1 | Pending | Not started | Tests pending |
| TST-024 | Test Report limitations/failures/open decisions | 11 | Report skeleton | Document validation | rc.1 | Pending | Not started | Final assessment pending |
| TST-025 | Test Report reproduction/evidence/runtime-non-change | 11 | Report skeleton | Document validation | rc.1 | Pending | Not started | Final evidence pending |
| REL-001 | Human versions/tags/JAR names use uppercase V | 12, 15 | Workflows/process | Workflow tests | All | N/A | Implemented | Release not executed |
| REL-002 | Only V0.0.x versions accepted | 12, 15 | Workflow regex | Negative-path tests | All | N/A | Implemented | Dispatch not executed |
| REL-003 | Gradle/plugin version strips leading V | 12 | Workflows | Workflow assertion | All | N/A | Implemented | Packaging runtime pending |
| REL-004 | `release_scope=core` required | 3, 12 | Workflows | Negative-path tests | All | N/A | Implemented | Other scopes fail closed |
| REL-005 | Pre-release/stable scope match | 12 | Stable workflow | Negative-path test | Stable | N/A | Implemented | Dispatch not executed |
| REL-006 | Environment approval retained | 12, 15 | Workflows | YAML assertion | All | N/A | Implemented | Approval not exercised |
| REL-007 | Explicit `requirements_cleared=true` | 12, 15 | Stable workflow | Negative-path test | Stable | N/A | Implemented | Owner-controlled |
| REL-008 | Traceability gate `CLEARED` required | 12, 13 | Stable workflow | Negative-path test | Stable | N/A | Implemented | Current marker BLOCKED |
| REL-009 | Release readiness `READY` required | 12, 13 | Stable workflow | Negative-path test | Stable | N/A | Implemented | Current marker BLOCKED |
| REL-010 | Traceability/readiness are tracked fixed inputs | 12, 13 | Stable workflow | Path/tracking tests | Stable | N/A | Implemented | Release not executed |
| REL-011 | Traceability/readiness copied, hashed, manifested | 12, 13 | Stable workflow | Packaging test | Stable | N/A | Implemented | Release not executed |
| REL-012 | Runtime JAR and SHA-256 | 12 | Workflow | Packaging test | beta.1+ | N/A | Not started | No release artifact |
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
