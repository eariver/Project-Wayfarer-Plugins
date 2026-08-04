# V0.0.2 Decision Register

This register records the Owner-resolved Phase 01–06 outcomes and the Phase 08B remediation
decisions separately from the immutable mainline requirement. It does not promote an Owner
Amendment into the original requirement. Phase 09B records the external Project/Frontier
decision for the first Client Test without treating it as a production promotion.

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
| FRONT-D02 | The LeafGrapple adapter uses the reviewed public capability boundary and fails closed for unsafe/unavailable configuration. The exact Safe Tier Fixture is approved for the first Client Test only; production balance remains open after that test. | `ACCEPTED_WITH_LIMITATION` |
| FRONT-D04 | Native Bukkit paths, public WorldGuard `RegionQuery`, and public WorldEdit `EditSession` paths are covered; unsupported bypassing tools are not claimed. | `ACCEPTED_WITH_LIMITATION` |
| MAIN-D08 | Native repair boundaries and reviewed supported cancellable external boundaries are covered; unsupported external repair paths are not claimed. | `ACCEPTED_WITH_LIMITATION` |
| Main death/reissue | Growth Tool/Broken Tool remains a normal Death Drop. Death does not reopen delivery, rotate authority, change delivery status, or debit Waymark; Player paid reissue and free pending-delivery retry remain separate. | `SUPERSEDED_BY_PHASE_10C_A` |
| Frontier death | Elytra, Grappling Hook, and Navigation use same-identity/epoch durable free redelivery; Launchpad and Rocket are excluded. | `DONE` |
| Progress overflow | Positive addition saturates at `Long.MAX_VALUE`; terminal threshold evaluation is idempotent. | `DONE` |

## Phase 10C-A Revision B Owner authority

The following is the tracked Owner decision layer for Candidate-4. It amends the
V0.0.2 Owner Bind and Frontier readiness behavior without rewriting the immutable
mainline requirement or changing Project Wayfarer Runtime authority.

| ID | Owner-resolved outcome | Status |
|---|---|---|
| MAIN-C4-OWNER-BIND | Owner may manually Drop/Pickup the managed Growth Tool, transfer it to another Player, move it through normal inventory and ordinary containers, and retain it through Death Drop. Non-owner physical possession and stale physical items remain physical items; use authorization is separate. | `ACCEPTED_FOR_CANDIDATE_4` |
| MAIN-C4-HOLD-AUTH | Hold-time authorization is resolved asynchronously into a Main-thread Session Cache. The full Owner/Tool ID/Epoch/Schema/Status comparison runs on Main Hand or authority availability/change/rewrite boundaries; ordinary use reads only the cache and fails closed when unavailable. | `ACCEPTED_FOR_CANDIDATE_4` |
| MAIN-C4-INVALID-USE | Non-owner or stale Break, Progress, GUI, Repair, Branch, and debug use is denied. Mending, external repair, Item Frame, and Armor Stand paths are denied. | `ACCEPTED_FOR_CANDIDATE_4` |
| MAIN-C4-PROCESSING | Identity-changing processing inventories (Anvil, Grindstone, Smithing, Crafting repair, Stonecutter, Cartography, Loom, Enchanting, and Beacon) deny managed-item processing. Ordinary container movement remains allowed. | `ACCEPTED_FOR_CANDIDATE_4` |
| MAIN-C4-DELIVERY-UX | Delivery success is announced only after physical insertion and durable `DELIVERED` commit. Exact message: `[Wayfarer] Growth Tool「Wayfarer Growth Pickaxe」を受け取りました。` No success message is emitted for present, pending, full, offline, wrong, conflict, or unknown outcomes. | `ACCEPTED_FOR_CANDIDATE_4` |
| MAIN-C4-NAMES | The exact managed names are `Wayfarer Growth Pickaxe`, `Broken Wayfarer Growth Pickaxe`, `Beyond Wayfarer Elytra`, `Beyond Wayfarer Grappling Hook`, and `Beyond Wayfarer Navigation`. Presentation does not replace PDC/DB identity. | `ACCEPTED_FOR_CANDIDATE_4` |
| FRONT-C4-READINESS | Safe Entry permits at most 40 bounded observations, requires two stable observations, and treats zero required items as stable only after two observations. One late MVI public event may restart the same external entry cycle after timeout; no recursion, repeating, or unbounded retry. | `ACCEPTED_FOR_CANDIDATE_4` |
| FRONT-C4-CLEANUP | Frontier self-heal removes only exact-current duplicate Elytra, Grappling Hook, and Navigation identities. Launchpad, Rocket, ordinary items, lookalikes, and incomplete metadata are not cleanup targets. | `ACCEPTED_FOR_CANDIDATE_4` |
| CANDIDATE-3-OUTCOME | Candidate-3 remains preserved as rejected evidence because Frontier duplicate self-heal did not complete and exact timing evidence was unavailable. | `REJECTED_PRESERVED` |
| CANDIDATE-4-OUTCOME | Candidate-4 Product Code and fixed artifacts are prepared before another focused Client Test. Candidate-4 remains distinct from Candidate-3 artifact and metadata evidence. | `PREPARED_FOR_FOCUSED_CLIENT_RETEST` |
| FRONT-D02-RESOURCE-PACK | Resource Pack work is not part of this Candidate-4 scope. | `SKIPPED_OUT_OF_SCOPE_BY_OWNER` |

The exact Candidate-3 input identities and failure chronology are recorded in
`.ai-work/luna-gpt-5.6-v003/reports/PHASE_10C_A_INPUT_AND_C3_FAILURE_TIMELINE.md`.

## Phase 09B Frontier concrete Fixture authority

The following external Project/Frontier decision is the authority for the first Client Test
baseline. It is not a Plugin, Luna, or ChatGPT self-approval.

```text
Project Issue:
  eariver/Project_Wayfarer#4

Approval comment:
  https://github.com/eariver/Project_Wayfarer/issues/4#issuecomment-5155937809
  Comment ID: 5155937809

Concrete Fixture:
  ACKNOWLEDGED

First Client Test baseline:
  APPROVED

Candidate:
  V0.0.2-Client-Candidate-1

Use:
  Client Test only

Pre-test balance changes:
  NONE

Production promotion:
  DECIDE_AFTER_CLIENT_TEST
```

The approved first-test movement/range/cooldown values are `max-distance=16.0`,
`max-pull-distance=32.0`, `launch-speed=1.2`, `pull-acceleration=0.05`,
`max-pull-speed=0.85`, and `cooldown-ticks=20`. The immutable Fixture is
`docs/testing/fixtures/V0.0.2/leafgrapple-wayfarer-test-only.yml` at commit
`521a41bbcc4d4e0e58111deeb663f52bf1c6e1af`, SHA-256
`ed210f8e56db26315f91fecb9e1d35d686c8fe647480498b7588467a6fa2448a`.

The LeafGrapple Fixture owns tier, item/model/display, movement, durability-disabled, and
entity-hook-disabled configuration. The Wayfarer Runtime Guard separately enforces the exact
case-sensitive `frontier_iris` boundary, rejects other Themes, remains enabled when that world is
absent or unloaded, never creates worlds, and leaves world creation/loading to Multiverse.

## Phase 09A/09B candidate state and remaining gates

| Gate | State | Boundary |
|---|---|---|
| Phase 09A independent review | `PASS` | The first Client Test Candidate and its fixed handoff were independently reviewed. |
| FRONT-D02 client gate | `CLIENT_TEST_REQUIRED` | The exact pinned LeafGrapple 1.0.2 Fixture baseline is resolved/approved for the first Client Test; bounded client motion remains. |
| Client Test Candidate | `FIXED` | `V0.0.2-Client-Candidate-1`; exact product source and local staged artifact hashes are recorded. |
| Bounded Client Acceptance | `NOT STARTED` | Mainline disposable Client Test preparation is pending Phase 09B independent review; detailed procedure is in the Client Acceptance Plan. |
| Project acceptance | Pending | Project-owned Runtime evidence. |
| Stable publication | Not authorized in this task | Tag, release, release hashes, `requirements_cleared`, and dispatch remain later work. |

LeafGrapple first-test baseline: `RESOLVED / APPROVED`.

LeafGrapple production balance: `OPEN_AFTER_CLIENT_TEST`.

Waystone and the EM–MVI adapter remain deferred/not authorized, not open V0.0.2 choices.

## Phase 10C-A Revision B candidate state

The Phase 09A/09B Candidate-1 record above is historical. The current handoff state is:

| Gate | State | Boundary |
|---|---|---|
| Candidate-3 | `REJECTED_PRESERVED` | Frontier duplicate self-heal failed; exact timing cause is unresolved. |
| Candidate-4 Product Code | `PREPARED` | Product HEAD is `9fe86d2e787ab1f86dcf38a5abdba6168515a802`; Product tests, Module Test, `check`, and `clean assemble` are green. |
| Candidate-4 artifact | `FIXED_PREPARED_FOR_FOCUSED_CLIENT_RETEST` | Published Core authority, approved Fixture, two Main/Frontier clean-build copies, manifest, and lowercase checksum are fixed; Main/Frontier/Fixture are byte-identical across builds. |
| Focused Client Test | `NOT_STARTED` | Owner-assisted Minecraft client operation is the next boundary. |
| Full Client Acceptance | `NOT_COMPLETE` | No client scenarios have started. |
| Production balance promotion | `HOLD` | Client and Project evidence are pending. |
| Project acceptance | `PENDING` | Project-owned Runtime evidence is not inferred. |
| Stable publication | `NOT_AUTHORIZED` | No tag/release/requirements clearance/dispatch is authorized. |

## Scope note

Phase 07's documentation-only history remains retained. Phase 08B adds only the focused
Frontier expiration guard, representative regression evidence, and synchronized handoff text.
Phase 09A adds candidate metadata and one sanitized test-only LeafGrapple fixture; it does not
change product bytes. The immutable requirement and `source.md` remain unchanged; Project
Runtime, migrations, configuration, worlds, inventories, tags, releases, and acceptance state
remain outside this task.
