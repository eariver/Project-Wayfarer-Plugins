# V0.0.2 Requirement Compliance

Product implementation anchor: `7faf79081572df028a5ec19ccfbc820123180fc7`.
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
| FRONT-D01 | `PLUGIN_REVIEW_REQUIRED` | Missing-world policy remains open |
| FRONT-D02 | `EXTERNAL_BLOCKED` / `CLIENT_TEST_REQUIRED` | LeafGrapple safe tier and client motion |
| FRONT-D04 | `PLUGIN_REVIEW_REQUIRED` | External protection coverage |
| MAIN-D08 | `PLUGIN_REVIEW_REQUIRED` | External repair guard matrix |
| Client Test Candidate / Client Acceptance | `CLIENT_TEST_REQUIRED` | Candidate not fixed; bounded client run remains later |
| Project acceptance | `PENDING` | Project Runtime evidence is outside this repository |
| Stable publication | `NOT_AUTHORIZED` | No tag, release, artifact hash, workflow dispatch, or completion claim |

Waystone and EM–MVI remain deferred/not authorized. `requirements_cleared` is absent and not
inferred. The row-level mapping is in `traceability.md`.
