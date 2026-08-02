# V0.0.2 Requirement Traceability

Classification: Phase 09A Plugin candidate handoff at product-source anchor
`90c3f5fe0f02fe297bd6d12f596ce6c9bac27cce` for Draft PR #14. The later candidate-fixation
metadata HEAD is documentation-only and does not change product bytes.

The immutable mainline requirement SHA-256 is
`2AD3CFB8AE54CA2149D8EABA44CBBC32470383787C35DB7C458704F87C67167F`.
Owner Amendments are documented as a separate decision layer; they do not rewrite the immutable
requirement. `requirements_cleared` is absent and is not inferred.

| Requirement / area | Implementation path | Automated or source evidence | Headless / client / Project evidence | State | Notes |
|---|---|---|---|---|---|
| Governance and authority | `docs/decisions/V0.0.2/`, `docs/work-orders/V0.0.2/` | Hash and Git boundary checks | Project Runtime unchanged | `DONE` | Phase 08B keeps Owner Amendments separate from the immutable requirement. |
| Core compatibility and migrations | Core API, V001–V003 | Compatibility and immutable migration tests | None required here | `DONE` | Core V0.0.1 is reused unchanged. |
| Main progress | Main progress/threshold domain | Saturation and terminal idempotency tests | Client mining later | `DONE` | Positive addition saturates at `Long.MAX_VALUE`; no negative wrap. |
| Main death/reissue | Main death policy, V004, reissue coordinator/commands | Death, quote, payment, rotation, pending, recovery tests | Client death/reissue later | `DONE` | No automatic respawn restoration; paid reissue; pending retry is free. |
| Main transaction recovery | Core transaction boundary and Main recovery | Idempotency/CAS and recovery tests | Project provider verification later | `DONE_WITH_LIMITATION` | `UNKNOWN` is explicit/manual and never auto-redebited or retried. |
| Main permissions | Main descriptor and leaf policy | Phase 06 descriptor/policy tests | LuckPerms Project check later | `DONE` | Old exact broad node is inactive. |
| Frontier permanent-item death | Typed durable Pending Delivery and Safe Entry/respawn reconciliation | Delivery/death identity and persistence tests | Client redelivery later | `DONE` | Elytra/Grappling Hook/Navigation redeliver free at same identity/epoch. |
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
| Client Test Candidate | Exact staged Core/Main/Frontier artifacts and candidate manifest | SHA-256, descriptor, and checksum evidence | Bounded client run required | `FIXED` | Label `V0.0.2-Client-Candidate-1`; Client Acceptance has not started. |
| Client Acceptance | `docs/testing/plans/V0.0.2-client-acceptance.md` | Candidate and Safe Tier handoff | Mainline disposable client environment | `CLIENT_TEST_REQUIRED` | No client result has been produced. |
| Project acceptance / Stable publication | Handoff and readiness docs | No release/tag/hash/dispatch | Project-owned | `PENDING` | No Client/Project acceptance, Stable Release, or `requirements_cleared`. |

Evidence types are intentionally separated: source/test evidence, headless evidence, client
observation, and Project acceptance are not interchangeable.

The V0.0.2 world name is fixed to exact case-sensitive `frontier_iris`; future single-name
configurability is tracked by Issue #17 and has no V0.0.2 implementation. The complete pinned
LeafGrapple Safe Tier is tracked at
`docs/testing/fixtures/V0.0.2/leafgrapple-wayfarer-test-only.yml`; it is test-only and not
Project runtime configuration.
