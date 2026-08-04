# V0.0.2 Requirement Compliance

Candidate-5 product-source anchor: `3ba94dd561e2f845fd7726329bd89cdbfb51d51a`.
Candidate-4 Product HEAD `9fe86d2e787ab1f86dcf38a5abdba6168515a802` is rejected before Client
Test and retained as historical evidence.
Requirement SHA-256: `2AD3CFB8AE54CA2149D8EABA44CBBC32470383787C35DB7C458704F87C67167F`.
Owner Amendments are a separate layer; the immutable requirement and `source.md` are unchanged.

| Workstream | Compliance state | Evidence / gate |
|---|---|---|
| Core V0.0.1 and V001–V003 | `DONE` | Immutable API/migration compatibility evidence |
| Main progress saturation | `DONE` | `Long.MAX_VALUE` saturation and terminal-idempotency tests |
| Main death/reissue and Main V004 | `DONE` | No auto-restore, paid reissue, rotation, pending/free retry, and recovery evidence |
| Frontier durable death redelivery | `DONE` | Typed Pending Delivery, same identity/epoch, Safe Entry/reconnect reconciliation |
| Phase 06 permission split | `DONE` | Leaf mapping, umbrella children, descriptor defaults, and no broad-node tests |
| Launchpad / portal scope | `DONE_WITH_LIMITATION` | Current-config/current-view Launchpad behavior; exact PlayerPortalEvent boundary |
| B-001 / B-004 | `DONE` | Module-local persistence and durable recovery model; no cross-store exactly-once claim |
| FRONT-D01 | `ACCEPTED_V0.0.2` | Unloaded-world expiration defers before destructive DB/index/block transition; plugin remains enabled and does not create worlds |
| FRONT-D02 | `ACCEPTED_WITH_LIMITATION` / `CLIENT_TEST_REQUIRED` | Public 1.0.2 adapter boundary is accepted; temporary safe tier and bounded client motion remain, with final motion/range/balance Mainline/Frontier-owned |
| FRONT-D04 | `ACCEPTED_WITH_LIMITATION` | Native Bukkit, public WorldGuard `RegionQuery`, and public WorldEdit `EditSession`; unsupported bypasses excluded |
| MAIN-D08 | `ACCEPTED_WITH_LIMITATION` | Native repair boundaries and supported cancellable external boundaries; unsupported external paths excluded |
| Phase 08B remediation review | `PASS` | External ChatGPT independent review accepted the focused correction and exact-head evidence |
| Candidate-5 Product remediation | `PRODUCT_PASS` | Tests-first RED/green, local validation, two clean builds, Normal CI `30774052884`, and Pre-client Headless `30774053040` at Product HEAD `3ba94dd` |
| Client Test Candidate | `PREPARED_FOR_FOCUSED_CLIENT_RETEST` | `V0.0.2-Client-Candidate-5`; exact staged Core/Main/Frontier hashes are in the Candidate-5 handoff; Client Test has not started |
| Client Acceptance | `CLIENT_TEST_REQUIRED` | Candidate-5 fixed; bounded client run has not started |
| Project acceptance | `PENDING` | Project Runtime evidence is outside this repository |
| Stable publication | `NOT_AUTHORIZED` | No tag, release, artifact hash, workflow dispatch, or completion claim |

Waystone and EM–MVI remain deferred/not authorized. The exact V0.0.2 world name remains
`frontier_iris`; future configurability is Issue #17 and has no V0.0.2 implementation. The
test-only LeafGrapple Safe Tier handoff passed and is recorded at
`docs/testing/fixtures/V0.0.2/leafgrapple-wayfarer-test-only.yml`; it is not Project runtime
configuration or a production balance recommendation.
`requirements_cleared` is absent and not inferred. The row-level mapping is in `traceability.md`.
