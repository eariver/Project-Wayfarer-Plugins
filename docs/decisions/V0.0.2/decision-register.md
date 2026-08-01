# V0.0.2 Decision Register

This register records the Owner-resolved Phase 01–06 outcomes separately from the immutable
mainline requirement. It does not promote an Owner Amendment into the original requirement.

Product implementation anchor: `7faf79081572df028a5ec19ccfbc820123180fc7`

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
| Main death/reissue | Growth Tool/Broken Tool is removed from death drops; no automatic restore; Player may use paid reissue; pending physical delivery retries free. | `DONE` |
| Frontier death | Elytra, Grappling Hook, and Navigation use same-identity/epoch durable free redelivery; Launchpad and Rocket are excluded. | `DONE` |
| Progress overflow | Positive addition saturates at `Long.MAX_VALUE`; terminal threshold evaluation is idempotent. | `DONE` |

## Remaining gates

| Gate | State | Boundary |
|---|---|---|
| FRONT-D01 | `PLUGIN_REVIEW_REQUIRED` | Missing `frontier_iris` behavior remains a review item. |
| FRONT-D02 | `EXTERNAL_BLOCKED`, then `CLIENT_TEST_REQUIRED` | LeafGrapple safe tier/capability and client motion. |
| FRONT-D04 | `PLUGIN_REVIEW_REQUIRED` | External protection coverage and bypass limitation. |
| MAIN-D08 | `PLUGIN_REVIEW_REQUIRED` | External repair guard matrix. |
| Client Test Candidate | Not fixed | Must not be fixed in Phase 07. |
| Bounded Client Acceptance | `CLIENT_TEST_REQUIRED` | Later fixed candidate and task-only environment. |
| Project acceptance | Pending | Project-owned Runtime evidence. |
| Stable publication | Not authorized in this task | Tag, release, release hashes and dispatch remain later work. |

Waystone and the EM–MVI adapter remain deferred/not authorized, not open V0.0.2 choices.

## Scope note

Phase 07 synchronizes documentation and performs validation only. The immutable requirement and
`source.md` remain unchanged; Project Runtime, migrations, configuration, worlds, inventories,
tags, releases, and acceptance state are outside this task.
