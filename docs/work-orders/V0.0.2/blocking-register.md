# V0.0.2 Blocking Register

Owner-resolved Phase 01–06 outcomes are recorded as completed implementation/evidence work.
Only the following external, review, client, Project, or later-release boundaries remain active.

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

### FRONT-D01 — missing `frontier_iris`

- State: `PLUGIN_REVIEW_REQUIRED`.
- The missing-world behavior remains a review boundary; Phase 07 does not change source.

### FRONT-D02 — LeafGrapple

- State: `EXTERNAL_BLOCKED`, followed by `CLIENT_TEST_REQUIRED`.
- A reviewed safe LeafGrapple 1.0.2 tier and later client motion evidence are required.

### FRONT-D04 — external protection

- State: `PLUGIN_REVIEW_REQUIRED`.
- Native/public protection paths are documented; tools that bypass the supported API are not
  claimed covered.

### MAIN-D08 — external repair guards

- State: `PLUGIN_REVIEW_REQUIRED`.
- External repair plugins without a supported cancellable boundary remain a limitation.

### Client / Project / publication

- Client Test Candidate: not fixed.
- Bounded Client Acceptance: `CLIENT_TEST_REQUIRED`.
- Project acceptance: pending and Project-owned.
- Stable V0.0.2 tag/release, release hashes, and release dispatch: not authorized in Phase 07.

Waystone and EM–MVI remain deferred/not authorized, not open V0.0.2 choices. The immutable
requirement and `source.md` are protected.
