# Phase 10C-A Candidate-4 Pre-client Independent Review — Addendum

Recorded: 2026-08-03 JST

Authority:

```text
Primary review:
  docs/release-readiness/V0.0.2/
  phase-10c-a-candidate-4-preclient-independent-review.md

Candidate-4 Product HEAD:
  9fe86d2e787ab1f86dcf38a5abdba6168515a802

Verdict:
  Candidate-4 REJECTED BEFORE CLIENT TEST
  Candidate-5 REQUIRED
```

## 1. Additional confirmed handler defect

`MainGameplayRuntime.onInteract(PlayerInteractEvent)` reads the current Main-Hand item into `held`,
but does not classify whether `held` is a managed Growth/Broken Tool before using the cached Held
Authorization.

Current behavior:

```text
RIGHT_CLICK_AIR + MAIN_HAND
  -> read held item
  -> read cached authorization
  -> read loaded Growth Tool authority
  -> when tool exists and cached allowsGui() is true, open Growth Tool GUI
```

Consequences during the confirmed stale-cache transition window:

- a stale `VALID_ACTIVE_OWNER` or `VALID_BROKEN_OWNER` cache can open the Growth Tool GUI while an
  ordinary item is actually held;
- the same stale cache can open the GUI while a stale, non-owner, revoked, malformed, or old-epoch
  managed Tool is held;
- an invalid managed Tool with an invalid cached state returns without cancelling the managed
  interaction, contrary to the Revision B contract.

Required Candidate-5 correction:

```text
actual held item is not managed:
  do not interfere

actual held item is managed and cached state is invalid:
  cancel the managed interaction
  do not open GUI

actual held item is managed and cached state is VALID_ACTIVE_OWNER or VALID_BROKEN_OWNER:
  cancel interaction
  open GUI
```

The handler must not reparse the complete physical claim; classification plus the fail-closed cached
result remains the intended use-time boundary.

## 2. Command and mutation hardening

The confirmed cache-transition defect also affects command entry points that trust capabilities
without checking the actual current Main-Hand item.

Candidate-5 must require both:

1. the actual Main-Hand item is a managed Growth/Broken Tool of the expected operation class; and
2. the cached state permits the operation.

Minimum boundaries:

```text
Branch:
  actual managed Tool
  cached state exactly VALID_ACTIVE_OWNER

Debug held-item mutation:
  actual managed active Tool
  cached state exactly VALID_ACTIVE_OWNER

Repair snapshot / repair execution:
  actual managed Tool
  cached VALID_ACTIVE_OWNER or VALID_BROKEN_OWNER as appropriate
```

These checks are lightweight classification checks, not per-use full PDC/DB revalidation.

## 3. Required regressions

- ordinary item + stale valid cache does not open Growth Tool GUI;
- stale/non-owner managed item + stale valid cache cancels interaction and opens no GUI;
- invalid managed item interaction is cancelled;
- ordinary item interaction is untouched after full authorization resolves to `NO_MANAGED_ITEM`;
- Branch and debug mutation fail when the actual held item is ordinary or stale even if a stale valid
  cache is injected by the test;
- active current Tool remains functional after full authorization.

## 4. Verdict impact

```text
CANDIDATE-4:
  remains REJECTED

CANDIDATE-5:
  remains REQUIRED

CLIENT TEST:
  remains DO NOT START
```

This addendum does not change Product Code or Candidate artifacts.
