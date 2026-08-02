# V0.0.2 Requirement Compliance

Candidate product-source anchor: `90c3f5fe0f02fe297bd6d12f596ce6c9bac27cce`.
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
| Client Test Candidate | `FIXED` | `V0.0.2-Client-Candidate-1`; exact staged Core/Main/Frontier hashes are in the candidate handoff |
| Client Acceptance | `CLIENT_TEST_REQUIRED` | Candidate fixed; bounded client run has not started |
| Project acceptance | `PENDING` | Project Runtime evidence is outside this repository |
| Stable publication | `NOT_AUTHORIZED` | No tag, release, artifact hash, workflow dispatch, or completion claim |

Waystone and EM–MVI remain deferred/not authorized. The exact V0.0.2 world name remains
`frontier_iris`; future configurability is Issue #17 and has no V0.0.2 implementation. The
test-only LeafGrapple Safe Tier handoff passed and is recorded at
`docs/testing/fixtures/V0.0.2/leafgrapple-wayfarer-test-only.yml`; it is not Project runtime
configuration or a production balance recommendation.
`requirements_cleared` is absent and not inferred. The row-level mapping is in `traceability.md`.
