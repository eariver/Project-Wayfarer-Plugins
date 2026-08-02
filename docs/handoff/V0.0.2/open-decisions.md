# V0.0.2 Open Decisions

Only the following gates remain open. Resolved B-001, B-004, MAIN-D04/D05/D06/D07,
FRONT-D01/D03/D04/D05/D06/D08, and MAIN-D08's supported-boundary decision are not reopened
here. Phase 09B resolves the first-test LeafGrapple baseline from the external Frontier decision;
production balance remains open after the Client Test.

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

- FRONT-D02: the Plugin adapter/fail-closed source boundary is accepted with limitation. The
  exact test-only LeafGrapple 1.0.2 Fixture is `ACKNOWLEDGED` and the first Client Test baseline
  is `APPROVED` by external Project Issue
  [#4 approval comment](https://github.com/eariver/Project_Wayfarer/issues/4#issuecomment-5155937809).
  It uses `max-distance=16.0`, `max-pull-distance=32.0`, `launch-speed=1.2`,
  `pull-acceleration=0.05`, `max-pull-speed=0.85`, and `cooldown-ticks=20`, with durability and
  all entity targets disabled. Pre-test balance changes are `NONE`; production promotion is
  `DECIDE_AFTER_CLIENT_TEST`.
- Client Test Candidate-1: `CLIENT_TEST_FAIL`, rejected for promotion; exact local artifact
  hashes and failure evidence remain immutable historical evidence.
- Client Test Candidate-2: `PREPARED_FOR_FOCUSED_CLIENT_RETEST` from product source
  `f2281093a03c17be0b0e69004059dd7ccb072b1c`; exact local artifact hashes and focused gates are
  recorded in the Candidate-2 handoff.
- Bounded Client Acceptance: `NOT COMPLETE` / `CLIENT_TEST_REQUIRED`; focused retest and
  independent review remain pending. Resource Pack is `SKIPPED_OUT_OF_SCOPE_BY_OWNER`.

## Project and publication

- ChatGPT independent review of the Phase 08B remediation: `PASS` (external decision).
- Phase 09A independent review: `PASS`; Phase 09B executor handoff: prepared, independent review
  required.
- Project acceptance: pending and Project-owned.
- Stable V0.0.2 publication: not authorized in this task; no tag, release, release hash,
  `requirements_cleared`, or workflow dispatch is permitted here.

Waystone and EM–MVI remain deferred/not authorized, not open V0.0.2 implementation choices.
