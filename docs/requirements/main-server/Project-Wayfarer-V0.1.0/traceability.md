# Requirement Traceability

Status values are those defined by the received requirement. `Passed` is not used without
commit-pinned implementation and evidence. Runtime evidence remains pending until an isolated
test-server result includes the user's observations where required.

| Requirement ID | Requirement | Implementation | Automated test | Pre-release | Runtime evidence | Status |
|---|---|---|---|---|---|---|
| GOV-001 | Preserve requirement snapshot and provenance | `source.md`; received snapshot | Snapshot/hash review | N/A | N/A | Implemented |
| GOV-002 | Preserve Project authority and Runtime boundary | `assessment.md`; `AGENTS.md` | Documentation review | N/A | N/A | Implemented |
| GOV-003 | Conditional adapter only after `ADAPTER_REQUIRED` | `AGENTS.md`; assessment | Repository/packaging inspection planned | All V0.0.1 phases | N/A | Implemented |
| VER-001 | Use Plugin `V0.0.x` release line | release process and plan | Workflow validation planned | V0.0.1 series | N/A | Implemented |
| VER-002 | Uppercase `V` for tags, releases, docs, directories, JARs | workflows and release process | Workflow validation planned | V0.0.1 series | N/A | Implemented |
| VER-003 | Strip leading `V` only for Gradle/plugin version | workflows | Packaging inspection planned | V0.0.1 series | N/A | Implemented |
| SCOPE-001 | V0.0.1 publishes only `Wayfarer_Core` | workflows; artifact matrix | Packaging test planned | V0.0.1 series | N/A | Implemented |
| SCOPE-002 | Main/Frontier/conditional adapter excluded | workflows; artifact matrix | Forbidden-artifact check planned | V0.0.1 series | N/A | Implemented |
| REL-001 | Require `release_scope=core` | both release workflows | Workflow validation planned | V0.0.1 series | N/A | Implemented |
| REL-002 | Pre-release/stable scope must match | stable workflow manifest validation | Workflow validation planned | V0.0.1-rc.1 → V0.0.1 | N/A | Implemented |
| REL-003 | Preserve GitHub Environment approvals | `test-server-release`; `main-server-release` | Workflow review | V0.0.1 series | N/A | Implemented |
| REL-004 | Record source, hashes, config, migration, immutable evidence | workflows; handoff skeleton | Manifest/packaging test planned | V0.0.1 series | N/A | Implemented |
| REL-005 | Stable requires explicit requirements clearance | stable workflow | Workflow negative-path test planned | V0.0.1 | N/A | Implemented |
| ARCH-001 | Core must not depend on Main/Frontier | Gradle module boundary | Dependency test planned | beta.1 | N/A | Not started |
| ARCH-002 | Main and Frontier must not depend on each other | Gradle module boundary | Dependency test planned | beta.1 | N/A | Not started |
| DATA-001 | MariaDB is authoritative for approved Core persistence | Core persistence slice | MariaDB/migration/restart tests planned | alpha.2 | Pending | Not started |
| DATA-002 | Redis is not sole persistent authority | Core Redis slice | outage/reconnect tests planned | alpha.3 | Pending | Not started |
| DATA-003 | Do not persist normal inventory or edit external storage | architecture boundary | Schema/packaging inspection planned | beta.1 | Pending | Not started |
| THREAD-001 | No main-thread JDBC/Redis I/O | Core task bridge | thread-detection tests planned | alpha.2/alpha.3 | Pending | Not started |
| THREAD-002 | Reject callbacks after disable | lifecycle/task bridge | disable race test planned | alpha.1/alpha.3 | Pending | Not started |
| CORE-001 | Typed config and environment-secret resolution | lifecycle slice | validation/redaction tests planned | alpha.1 | Pending | Not started |
| CORE-002 | Fail-closed lifecycle and partial-init cleanup | lifecycle slice | failure/disable tests planned | alpha.1 | Pending | Not started |
| CORE-003 | Health foundation and admin health command | lifecycle slice | unit/permission tests planned | alpha.1 | Pending | Not started |
| CORE-004 | MariaDB pool and Flyway ownership | persistence slice | Testcontainers migration tests planned | alpha.2 | Pending | Not started |
| CORE-005 | Audit and player/item identity foundations | persistence slice | persistence/identity tests planned | alpha.2 | Pending | Not started |
| CORE-006 | Redis health and task/backpressure boundary | Redis/task slice | Redis/concurrency tests planned | alpha.3 | Pending | Not started |
| CORE-007 | Waymark capability probe | transaction slice | provider failure tests planned | alpha.4 | Pending | Not started |
| CORE-008 | Idempotent transaction/refund/reconcile state machine | transaction slice | duplicate/restart/timeout tests planned | alpha.4 | Pending | Not started |
| API-001 | Stable implementation-free public API | `wayfarer-api` slices | compatibility/class-identity tests planned | beta.1 | Pending | Not started |
| TEST-001 | Unit and configuration tests | phase plans | Gradle `check` | alpha.1+ | Pending | Not started |
| TEST-002 | MariaDB, migration, and restart tests | alpha.2 plan | Testcontainers | alpha.2 | Pending | Not started |
| TEST-003 | Redis, concurrency, timeout, shutdown tests | alpha.3 plan | Testcontainers | alpha.3 | Pending | Not started |
| TEST-004 | Idempotency and duplicate debit/refund tests | alpha.4 plan | provider fixture/Testcontainers | alpha.4 | Pending | Not started |
| TEST-005 | Packaging, API identity, and configuration-cache tests | beta.1 plan | Gradle/ZIP inspection | beta.1 | Pending | Not started |
| TEST-006 | Isolated runtime verification and user observations | phase result records | N/A | alpha.1–rc.1 | Pending | Not started |
| DOC-001 | Maintain phase plans/results and final test report | `docs/testing`; `docs/reports` planned | Documentation validation planned | All phases | Pending | Implemented |
| HANDOFF-001 | Repository-managed V0.0.1 handoff package | `docs/handoff/V0.0.1` | Documentation validation planned | rc.1/stable | Pending | Implemented |
| RUNTIME-001 | Do not change Project Runtime from this repository | `AGENTS.md`; release process | Git/artifact inspection planned | All phases | N/A | Implemented |
