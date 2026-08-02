# V0.0.2 Blocking Register

Owner-resolved Phase 01–06 outcomes are recorded as completed implementation/evidence work.
Phase 08B resolved the unloaded-world expiration defect. Phase 09A fixed the first bounded
Client Test Candidate and synchronized the remaining external, client, Project, and later-release
boundaries below.

## Resolved implementation boundaries

### B-001 / ADR 0009 — module persistence

- Status: `DONE`.
- Main and Frontier own bounded module-local pools and separate migration histories.
- Core V0.0.1 API and migrations remain unchanged.
- No Project Runtime database or migration operation is authorized here.

### B-004 — transaction/domain fulfillment

- Status: `DONE`.
- Durable module order, pending fulfillment, known-no-effect-only recovery, and `UNKNOWN`
  handling are implemented.
- Cross-store atomicity and unconditional exactly-once are not claimed.
- `UNKNOWN` is never automatically debited, refunded, or retried.

### Resolved Owner Amendment outcomes

- MAIN-D01/D02/D03 baselines are implemented.
- MAIN-D04/D05/D06 current English/presentation and sanitized text are accepted for V0.0.2;
  later tuning is deferred.
- MAIN-D07 Phase 06 permission split is implemented.
- FRONT-D03 current-config Launchpad performance plus minimal durable authority is adopted;
  stored yaw is reserved/non-authoritative.
- FRONT-D05 current English Navigation presentation is accepted for V0.0.2.
- FRONT-D06 typed durable Pending Delivery is implemented.
- FRONT-D08 is limited to `PlayerPortalEvent` cancellation in current world `frontier_iris`.
- Main death has no automatic restore and uses paid Player reissue; Frontier permanent items use
  durable free redelivery; progress saturates at `Long.MAX_VALUE`.

## Active gates

### FRONT-D01 — missing or unloaded `frontier_iris`

- State: `RESOLVED` for V0.0.2.
- The plugin remains enabled when `frontier_iris` is absent or unloaded and never creates worlds.
  A scheduler expiration candidate is deferred while the world is unavailable; after a later
  load, a relevant Join, WorldChanged, or Respawn path, the exact case-sensitive player boundary
  resumes. No `WORLD_DOWN`, `DEGRADED`, or new health subsystem is added.
- The Phase 08B regression proves that `UNKNOWN` cannot authorize the destructive expiration
  transition.

### FRONT-D02 — LeafGrapple

- State: Plugin source boundary `ACCEPTED_WITH_LIMITATION`; `CLIENT_TEST_REQUIRED` remains.
- The adapter targets LeafGrapple 1.0.2 through its public capability boundary and fails closed
  when unsafe or unavailable. A temporary test-only safe tier must copy movement/range/display
  values from a reviewed 1.0.2 standard tier and disable durability plus entity/player/mob/animal/
  monster hooking. It is not a production balance recommendation; final motion/range/balance is
  Mainline/Frontier-owned.

### FRONT-D04 — external protection

- State: `ACCEPTED_WITH_LIMITATION`.
- Coverage is claimed for native representative Bukkit events, public WorldGuard `RegionQuery`,
  and public WorldEdit `EditSession`. Tools bypassing Bukkit and public EditSession APIs are not
  claimed covered.

### MAIN-D08 — external repair guards

- State: `ACCEPTED_WITH_LIMITATION`.
- Native repair boundaries are guarded, and reviewed external paths are covered only when they
  expose a supported cancellable event or API boundary. Unsupported external repair plugins
  remain outside the claim.

### Client / Project / publication

- ChatGPT independent review of this remediation: `PASS` (external decision).
- Client Test Candidate: `FIXED` as `V0.0.2-Client-Candidate-1` from product source
  `90c3f5fe0f02fe297bd6d12f596ce6c9bac27cce`; exact local Core/Main/Frontier bytes and hashes are
  recorded in the Phase 09A handoff.
- LeafGrapple 1.0.2 pinned artifact and complete test-only `hooks.wayfarer` Safe Tier handoff:
  `PASS`; tracked fixture is `docs/testing/fixtures/V0.0.2/leafgrapple-wayfarer-test-only.yml`.
- Bounded Client Acceptance: `CLIENT_TEST_REQUIRED`.
- Project acceptance: pending and Project-owned.
- Stable V0.0.2 tag/release, release hashes, `requirements_cleared`, and release dispatch: not
  authorized in this task.

The V0.0.2 world name remains the exact case-sensitive `frontier_iris`. Future single-name
configurability is tracked by Issue #17; no V0.0.2 implementation was added.

Waystone and EM–MVI remain deferred/not authorized, not open V0.0.2 choices. The immutable
requirement and `source.md` are protected.
