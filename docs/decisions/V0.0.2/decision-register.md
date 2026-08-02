# V0.0.2 Decision Register

This register records the Owner-resolved Phase 01–06 outcomes and the Phase 08B remediation
decisions separately from the immutable mainline requirement. It does not promote an Owner
Amendment into the original requirement.

Accepted product-source anchor: `90c3f5fe0f02fe297bd6d12f596ce6c9bac27cce`

## Resolved / implemented

| ID / area | Fixed outcome | Status |
|---|---|---|
| B-001 / ADR 0009 | Main and Frontier use bounded module-local persistence and separate migration histories; Core remains unchanged. | `DONE` |
| B-004 | Durable module fulfillment and pending recovery reuse Core V0.0.1; cross-store atomicity and unconditional exactly-once are not claimed; `UNKNOWN` is manual and not auto-retried. | `DONE` |
| MAIN-D01/D02/D03 | Tunable progress, threshold, and repair baselines are implemented. | `DONE` |
| MAIN-D04 | Current English GUI/layout is accepted for V0.0.2; later presentation tuning is deferred. | `ACCEPTED_V0.0.2` |
| MAIN-D05 | Current Growth Tool name/lore is accepted for V0.0.2; later presentation tuning is deferred. | `ACCEPTED_V0.0.2` |
| MAIN-D06 | Current sanitized Player text is accepted for V0.0.2. | `ACCEPTED_V0.0.2` |
| MAIN-D07 | Phase 06 leaf permission split is implemented and documented. | `DONE` |
| FRONT-D03 | Current-config performance plus minimal durable Launchpad authority is adopted; reserved yaw is non-authoritative. | `ACCEPTED_V0.0.2` |
| FRONT-D05 | Current English Navigation presentation is accepted for V0.0.2; later presentation tuning is deferred. | `ACCEPTED_V0.0.2` |
| FRONT-D06 | Typed durable Pending Delivery is implemented and adopted; normal inventories remain backend/MVI-owned. | `DONE` |
| FRONT-D08 | `FrontierGameplayRuntime` cancels `PlayerPortalEvent` when the Player's current world is `frontier_iris`; no separate End Gateway interception is claimed. | `ACCEPTED_SCOPE` |
| FRONT-D01 | `frontier_iris` may be absent or unloaded without disabling the plugin or creating a world; expiration defers while unavailable and the exact player-triggered boundary resumes after a later load. No health/degraded state is added. | `ACCEPTED_V0.0.2` |
| FRONT-D02 | The LeafGrapple adapter uses the reviewed public capability boundary and fails closed for unsafe/unavailable configuration; a temporary safe tier and bounded client motion test remain. | `ACCEPTED_WITH_LIMITATION` |
| FRONT-D04 | Native Bukkit paths, public WorldGuard `RegionQuery`, and public WorldEdit `EditSession` paths are covered; unsupported bypassing tools are not claimed. | `ACCEPTED_WITH_LIMITATION` |
| MAIN-D08 | Native repair boundaries and reviewed supported cancellable external boundaries are covered; unsupported external repair paths are not claimed. | `ACCEPTED_WITH_LIMITATION` |
| Main death/reissue | Growth Tool/Broken Tool is removed from death drops; no automatic restore; Player may use paid reissue; pending physical delivery retries free. | `DONE` |
| Frontier death | Elytra, Grappling Hook, and Navigation use same-identity/epoch durable free redelivery; Launchpad and Rocket are excluded. | `DONE` |
| Progress overflow | Positive addition saturates at `Long.MAX_VALUE`; terminal threshold evaluation is idempotent. | `DONE` |

## Phase 09A candidate state and remaining gates

| Gate | State | Boundary |
|---|---|---|
| ChatGPT independent review | `PASS` | External independent review accepted the Phase 08B remediation and fixed product-source anchor. |
| FRONT-D02 client gate | `CLIENT_TEST_REQUIRED` | Exact pinned LeafGrapple 1.0.2 Safe Tier handoff is complete; bounded client motion remains. |
| Client Test Candidate | `FIXED` | `V0.0.2-Client-Candidate-1`; exact product source and local staged artifact hashes are recorded. |
| Bounded Client Acceptance | `CLIENT_TEST_REQUIRED` | Later fixed candidate and task-only environment. |
| Project acceptance | Pending | Project-owned Runtime evidence. |
| Stable publication | Not authorized in this task | Tag, release, release hashes, `requirements_cleared`, and dispatch remain later work. |

Waystone and the EM–MVI adapter remain deferred/not authorized, not open V0.0.2 choices.

## Scope note

Phase 07's documentation-only history remains retained. Phase 08B adds only the focused
Frontier expiration guard, representative regression evidence, and synchronized handoff text.
Phase 09A adds candidate metadata and one sanitized test-only LeafGrapple fixture; it does not
change product bytes. The immutable requirement and `source.md` remain unchanged; Project
Runtime, migrations, configuration, worlds, inventories, tags, releases, and acceptance state
remain outside this task.
