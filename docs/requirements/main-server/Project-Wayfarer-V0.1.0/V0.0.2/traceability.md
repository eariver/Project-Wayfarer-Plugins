# V0.0.2 Requirement Traceability

Classification: Phase 10C-A Candidate-5 Product remediation result. Candidate-5 Product HEAD is
`3ba94dd561e2f845fd7726329bd89cdbfb51d51a`; Product changes and focused tests are fixed at that
immutable commit. Candidate-4 was rejected before Client Test and remains preserved as historical
evidence; Candidate-5 Client Test has not started.

The immutable mainline requirement SHA-256 is
`2AD3CFB8AE54CA2149D8EABA44CBBC32470383787C35DB7C458704F87C67167F`.
Owner Amendments are documented as a separate decision layer; they do not rewrite the immutable
requirement. `requirements_cleared` is absent and is not inferred.

| Requirement / area | Implementation path | Automated or source evidence | Headless / client / Project evidence | State | Notes |
|---|---|---|---|---|---|
| Governance and authority | `docs/decisions/V0.0.2/`, `docs/work-orders/V0.0.2/` | Hash and Git boundary checks | Project Runtime unchanged | `DONE` | Phase 08B keeps Owner Amendments separate from the immutable requirement. |
| Core compatibility and migrations | Core API, V001–V003 | Compatibility and immutable migration tests | None required here | `DONE` | Core V0.0.1 is reused unchanged. |
| Main progress | Main progress/threshold domain | Saturation and terminal idempotency tests | Client mining later | `DONE` | Positive addition saturates at `Long.MAX_VALUE`; no negative wrap. |
| Main death/reissue | Main death policy, V004, reissue coordinator/commands | Death, quote, payment, rotation, pending, recovery tests | Client death/reissue later | `AMENDED_FOR_CANDIDATE_4` | Growth Tool/Broken Tool remains a normal Death Drop; death does not reopen delivery, rotate authority, change status, or debit Waymark. Paid reissue and free pending retry remain. |
| Main Owner Bind | `HeldGrowthToolAuthorization`, `HeldGrowthToolAuthorizer`, Main-thread Session Cache, inventory policy | Candidate-5 focused transition/authorization tests; Main 126-test suite | Focused client retest pending | `PRODUCT_PASS_CANDIDATE_5` / `CLIENT_TEST_REQUIRED` | Physical transfer is allowed; use authorization is fail-closed, cache-backed, invalidated across all Main-Hand-changing handler families, and exact-state guarded. |
| Main delivery presentation | Growth Tool presentation boundary and durable delivery callback | Presentation/message and delivery coordinator evidence | Focused client retest pending | `PRODUCT_PASS_CANDIDATE_5` / `CLIENT_TEST_REQUIRED` | Exact success text is emitted only after physical insertion and durable `DELIVERED`; ordinary items remain outside managed action paths. |
| Main transaction recovery | Core transaction boundary and Main recovery | Idempotency/CAS and recovery tests | Project provider verification later | `DONE_WITH_LIMITATION` | `UNKNOWN` is explicit/manual and never auto-redebited or retried. |
| Main permissions | Main descriptor and leaf policy | Phase 06 descriptor/policy tests | LuckPerms Project check later | `DONE` | Old exact broad node is inactive. |
| Frontier permanent-item death | Typed durable Pending Delivery and Safe Entry/respawn reconciliation | Delivery/death identity and persistence tests | Client redelivery later | `PRODUCT_PASS_CANDIDATE_5` / `CLIENT_TEST_REQUIRED` | Candidate-5 retains exact identity/epoch behavior; cleanup is limited to exact-current permanent identities. |
| Frontier Safe Entry readiness | Bounded readiness and same-cycle late MVI restart | Candidate-5 timeout diagnostic and late-MVI coordination tests; Frontier 82-test suite | Pre-client Headless run `30774053040` passed; focused client retest pending | `PRODUCT_PASS_CANDIDATE_5` / `CLIENT_TEST_REQUIRED` | Maximum 40 observations, two stable observations, zero-required stabilization, one late restart per external cycle; terminal diagnostics are bounded and sanitized. |
| Frontier duplicate cleanup | Exact-current Elytra/Hook/Navigation cleanup | Metadata/cleanup focused tests | Focused client retest pending | `PRODUCT_PASS_CANDIDATE_5` / `CLIENT_TEST_REQUIRED` | Launchpad, Rocket, ordinary items, lookalikes, and incomplete PDC are not targets. |
| Frontier Launchpad | Launchpad authority, current config, placement/use/reconcile, and unloaded-world expiration guard | Identity/replay/placement/protection tests; `LaunchpadExpirationDecisionTest` | Client motion/protection review later | `DONE_WITH_LIMITATION` | `UNKNOWN` defers the destructive expiration transition; current view direction at use time; stored yaw is reserved, not authoritative. |
| Frontier portal boundary | `FrontierGameplayRuntime` | Source/runtime event coverage | Nether/End/End Gateway client observation later | `ACCEPTED_SCOPE` | Cancels `PlayerPortalEvent` when current world is `frontier_iris`; no separate End Gateway interception. |
| Frontier permissions | Frontier descriptor and leaf policy | Phase 06 descriptor/policy tests | LuckPerms Project check later | `DONE` | No top-level command permission; debug is separate. |
| FRONT-D01 | Frontier missing-world policy | `FrontierGameplayRuntime` guard and `LaunchpadExpirationDecisionTest` | Later exact-world/player-entry observation | `ACCEPTED_V0.0.2` | Plugin remains enabled, never creates worlds, and defers `UNKNOWN`; no health/degraded subsystem. |
| FRONT-D02 | LeafGrapple 1.0.2 public capability boundary | Fail-closed capability tests | Temporary safe tier and client motion required | `ACCEPTED_WITH_LIMITATION` / `CLIENT_TEST_REQUIRED` | No fallback physics or private API claim; final motion/range/balance is Mainline/Frontier-owned. |
| FRONT-D04 | WorldGuard/WorldEdit/public protection boundary | Native/public hook tests | Unsupported bypass paths remain outside the claim | `ACCEPTED_WITH_LIMITATION` | Coverage is limited to native Bukkit, public `RegionQuery`, and public `EditSession`. |
| MAIN-D08 | External repair guard matrix | Native-first guard evidence | Unsupported external paths remain outside the claim | `ACCEPTED_WITH_LIMITATION` | External coverage requires a supported cancellable boundary. |
| Phase 08B remediation review | Focused source/test correction and exact-head evidence | Local/CI/Headless evidence | ChatGPT independent review | `PASS` | External decision; product-source anchor remains fixed. |
| Waystone | No production Waystone behavior | Absence/config/package checks | Not authorized | `DEFERRED_BY_REQUIREMENT` | Not an open V0.0.2 choice. |
| EM–MVI adapter | No module or artifact | Absence checks | Project decision required | `DEFERRED_BY_REQUIREMENT` | Create only after `ADAPTER_REQUIRED`. |
| Candidate-3 outcome | Candidate-3 review, failure timeline, and preserved submission | Exact input hashes, CI/headless evidence, failure chronology | No Candidate-3 promotion | `REJECTED_PRESERVED` | Frontier duplicate self-heal failed; exact timing cause is unresolved. |
| Candidate-4 outcome | Candidate-4 prepared/review documents and preserved artifacts | Product HEAD `9fe86d2e`; no Client Test started | No Candidate-4 promotion | `REJECTED_PRESERVED` | Candidate-4 was rejected by the Candidate-5 Execution Entry before Client Test. |
| Candidate-5 Product remediation | Product commit `3ba94dd561e2f845fd7726329bd89cdbfb51d51a` | Tests-first RED/green evidence, local check/assemble, two clean builds, Normal CI `30774052884`, Headless `30774053040` | Client Test not started | `PRODUCT_PASS` | Candidate-5 Main/Frontier bytes are fixed for later Client Test; Core published V0.0.1 authority remains separate and unchanged. |
| Client Test Candidate | Exact staged Core/Main/Frontier artifacts and Candidate-5 manifest | SHA-256, descriptor, checksum, two-clean-build evidence | Bounded client run required | `PREPARED_FOR_FOCUSED_CLIENT_RETEST` | Product HEAD `3ba94dd`; Main/Frontier/Fixture Build 1/2 are byte-identical; Published Core authority is separately verified. |
| Client Acceptance | `docs/testing/plans/V0.0.2-client-acceptance.md` | Candidate and Safe Tier handoff | Mainline disposable client environment | `CLIENT_TEST_REQUIRED` | No client result has been produced. |
| Project acceptance / Stable publication | Handoff and readiness docs | No release/tag/hash/dispatch | Project-owned | `PENDING` | No Client/Project acceptance, Stable Release, or `requirements_cleared`. |

Evidence types are intentionally separated: source/test evidence, headless evidence, client
observation, and Project acceptance are not interchangeable.

The V0.0.2 world name is fixed to exact case-sensitive `frontier_iris`; future single-name
configurability is tracked by Issue #17 and has no V0.0.2 implementation. The complete pinned
LeafGrapple Safe Tier is tracked at
`docs/testing/fixtures/V0.0.2/leafgrapple-wayfarer-test-only.yml`; it is test-only and not
Project runtime configuration.
