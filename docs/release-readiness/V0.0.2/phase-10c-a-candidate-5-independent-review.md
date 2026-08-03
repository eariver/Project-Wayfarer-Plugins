# Phase 10C-A Candidate-5 Independent Review

Recorded: 2026-08-03 JST

## 1. Reviewed authority

```text
Repository:
  eariver/Project-Wayfarer-Plugins

PR:
  #14 / Open / Draft / Unmerged

Candidate-5 Product HEAD:
  3ba94dd561e2f845fd7726329bd89cdbfb51d51a

Candidate-5 status HEAD reviewed:
  81046d09ed561ecc75345f22a71072db70c8f87d

Minecraft Client Test:
  NOT STARTED
```

The review considered the Product diff, the tracked result and runtime handoff, the successful
Normal CI and Pre-client Headless runs, the Main authorization implementation and focused tests, and
the Frontier timeout/late-MVI implementation and tests.

## 2. Confirmed successful work

Candidate-5 correctly closes several Candidate-4 findings:

- `VALID_BROKEN_OWNER` no longer permits Branch mutation;
- actual managed-item classification is present at the principal managed-action boundaries;
- held-slot and other inventory mutation handlers use a fail-closed transition before deferred
  Main-Hand reauthorization;
- Frontier terminal TIMEOUT evidence now contains source, generation, poll count, visible/required
  managed counts, fingerprint, and decision;
- the timeout-then-public-MVI path has a concrete runtime-level test for one restart, duplicate
  coalescing, and same-cycle reuse;
- Product CI and Pre-client Headless completed successfully;
- two recorded clean builds produced matching Main and Frontier bytes;
- Runtime and Minecraft Client operations were not started from the Plugin repository context.

These successes remain useful Candidate-6 baseline evidence.

## 3. Confirmed blocking Product defect — asynchronous authority mutation is not fail-closed early enough

`MainGameplayRuntime.replaceAuthority(...)` dispatches the complete Reissue/Revoke database
operation before invalidating the cached Held Authorization. Invalidation occurs only later in the
main-thread continuation after a successful mutation result is returned.

Therefore the following interval exists:

```text
cached state:
  VALID_ACTIVE_OWNER or VALID_BROKEN_OWNER for the old authority

database task:
  performs durable Reissue/Revoke and may commit a new Item Instance / Epoch / status

before main-thread continuation:
  old cached capability remains observable by Bukkit actions
```

This violates the Candidate-5 invariant that an old authorization must become unusable before a
changed authority or identity can be observed.

The conflict/failure path is also unsafe:

- `CONFLICT` returns with `mutation.tool() == null` and performs no invalidation, reload, or full
  reauthorization;
- an exceptional database completion maps to `UNAVAILABLE` without restoring a cache for the
  still-authoritative current row;
- when another writer won the conflict, the local cache can remain tied to the old Instance/Epoch
  indefinitely.

`refreshSession(...)` has the same ordering issue: it performs the authoritative database read first
and invalidates only in the main-thread continuation. When refresh is requested because authority
may have changed externally, the old cached capabilities remain usable during the read.

Required invariant:

1. on the main thread, make the old cached authorization unusable before dispatching an asynchronous
   authority mutation or refresh whose result can expose changed authority;
2. perform no synchronous database or Redis access in Bukkit handlers;
3. on success, install the new authoritative Session state and fully authorize the actual Main-Hand
   item;
4. on conflict, failure, cancellation, or no-change, load or retain the actual still-authoritative
   state and fully reauthorize it;
5. never restore a cache produced for an older Item Instance or Epoch;
6. never leave an online Player permanently unavailable solely because the asynchronous operation
   failed.

This requires Product Code changes after Candidate-5 fixation.

## 4. Main automated-proof gap

The Candidate-5 Product commit adds:

- one Broken-owner Branch capability assertion; and
- one runtime transition test for `PlayerItemHeldEvent`.

It does not add focused runtime proof for the other changed handler families or for asynchronous
Reissue/Revoke/refresh ordering.

Candidate-6 must add bounded representative tests for:

- hand swap;
- accepted inventory click/number-key and drag;
- Drop/Pickup/Respawn transition behavior;
- ordinary-item behavior after full `NO_MANAGED_ITEM` authorization;
- actual-item protection for GUI/Branch/Repair or equivalent security-relevant entry points;
- immediate fail-close before an incomplete database future can complete;
- success, conflict, and failure recovery without restoring an old Epoch cache.

An exhaustive event-by-capability Cartesian product is not required. Every changed handler family
and every distinct security invariant must be represented.

## 5. Frontier automated-proof gap

The new Frontier runtime test proves:

- bounded TIMEOUT;
- a qualifying public MVI observation;
- one scheduled restart;
- duplicate-event coalescing; and
- same-cycle reuse after running the restart.

It does not prove the required cancellation behavior for:

- Player quit;
- actual Frontier world leave;
- a newer external entry cycle; and
- plugin/runtime stop.

The current source appears to contain cancellation paths, so this is recorded as an automated-proof
gap rather than a confirmed Frontier Product defect. Candidate-6 must add focused runtime-level tests
for these four boundaries. Product code should change only when a test exposes an actual defect.

## 6. Evidence-package boundary

The tracked result states that the sanitized Candidate-5 submission ZIP and sidecar are local-only.
They were not available through the repository or workflow artifacts reviewed here, so their ZIP
integrity, internal checksum coverage, references, and external sidecar have not been independently
verified.

The package must be regenerated for Candidate-6. The final Candidate-6 package must be supplied to
the independent reviewer as actual bytes before Runtime authorization.

## 7. CI and reproducibility interpretation

The successful Product workflows are valid PR merge-ref evidence whose PR-head parent is Candidate-5
Product HEAD. They are not direct Product-HEAD checkouts. The CI job also executed the repository
integration suites in isolated workflow services, closing the local exclusion limitation for the
tested merge-ref tree.

The recorded Candidate-5 Main and Frontier two-build identities remain historical evidence, but the
confirmed Product defect requires new Candidate-6 Product bytes and a new two-formal-build fixation.

## 8. Independent verdict

```text
PHASE 10C-A CANDIDATE-5 INDEPENDENT REVIEW:
  FAIL / HOLD

CANDIDATE-5:
  REJECTED BEFORE RUNTIME PREFLIGHT

CONFIRMED PRODUCT DEFECT:
  ASYNCHRONOUS AUTHORITY MUTATION / REFRESH FAIL-CLOSE ORDERING

MAIN AUTOMATED PROOF:
  INCOMPLETE

FRONTIER LATE-MVI CANCELLATION PROOF:
  INCOMPLETE

CANDIDATE-5 SUBMISSION PACKAGE:
  NOT INDEPENDENTLY VERIFIED

CANDIDATE-6:
  REQUIRED

RUNTIME PREFLIGHT:
  DO NOT START

CLIENT TEST:
  DO NOT START

FULL CLIENT ACCEPTANCE:
  NOT COMPLETE

PRODUCTION BALANCE PROMOTION:
  HOLD

PROJECT ACCEPTANCE:
  PENDING

STABLE PUBLICATION:
  NOT AUTHORIZED
```

## 9. Candidate-6 scope

Candidate-6 is limited to:

1. make asynchronous Reissue/Revoke/refresh fail-closed before database dispatch;
2. restore exact current authorization after success, conflict, no-change, or failure;
3. add representative Main handler-family and async-ordering tests;
4. add Frontier late-MVI cancellation tests and change Frontier Product only if a test fails;
5. preserve all Candidate-5 fixes and accepted behavior;
6. rerun focused/full validation, CI, and Headless;
7. perform two new formal clean builds from exact Candidate-6 Product HEAD;
8. create immutable Candidate-6 artifacts and a complete sanitized package;
9. stop before Runtime or Minecraft Client operations for another independent review.

Candidate-5 fixed artifacts remain immutable historical evidence and must not be overwritten.
