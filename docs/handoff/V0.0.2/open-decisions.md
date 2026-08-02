# V0.0.2 Open Decisions

Only the following gates remain open. Resolved B-001, B-004, MAIN-D04/D05/D06/D07,
FRONT-D01/D03/D04/D05/D06/D08, and MAIN-D08's supported-boundary decision are not reopened
here.

## Phase 08B resolved Plugin decisions

- FRONT-D01: resolved. The plugin remains enabled when `frontier_iris` is absent or unloaded,
  creates no world, and defers Launchpad expiration until the world can be classified. The exact
  player-triggered boundary resumes after a later load/relevant entry; no health/degraded state
  is added.
- FRONT-D04: accepted with limitation for native Bukkit events, public WorldGuard `RegionQuery`,
  and public WorldEdit `EditSession`; bypassing tools are outside the claim.
- MAIN-D08: accepted with limitation for native repair boundaries and external paths with a
  supported cancellable boundary; unsupported external paths are outside the claim.
- V0.0.2 keeps the exact case-sensitive world name `frontier_iris`. Future single-name
  configurability is Issue #17 and is not implemented here.

## External and client

- FRONT-D02: the Plugin adapter/fail-closed source boundary is accepted; provide/approve a
  temporary test-only safe LeafGrapple 1.0.2 tier, then perform bounded client motion checks.
  Copy movement/range/display values from a reviewed 1.0.2 standard tier, disable durability and
  entity/player/mob/animal/monster hooking, and do not treat the tier as a production balance
  recommendation.
- Client Test Candidate: `V0.0.2-Client-Candidate-1` fixed from product source
  `90c3f5fe0f02fe297bd6d12f596ce6c9bac27cce`; exact local artifact hashes and Safe Tier evidence
  are recorded in the Phase 09A handoff.
- Bounded Client Acceptance: `CLIENT_TEST_REQUIRED` after a later candidate is fixed.

## Project and publication

- ChatGPT independent review of the Phase 08B remediation: `PASS` (external decision).
- Project acceptance: pending and Project-owned.
- Stable V0.0.2 publication: not authorized in this task; no tag, release, release hash,
  `requirements_cleared`, or workflow dispatch is permitted here.

Waystone and EM–MVI remain deferred/not authorized, not open V0.0.2 implementation choices.
