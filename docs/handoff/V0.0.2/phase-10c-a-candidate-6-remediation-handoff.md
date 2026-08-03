# Phase 10C-A Candidate-6 Remediation Handoff

Revision: A  
Recorded: 2026-08-03 JST

## 1. Purpose and authority

Candidate-5 was rejected before Runtime Preflight by:

`docs/release-readiness/V0.0.2/phase-10c-a-candidate-5-independent-review.md`

This handoff authorizes one narrow Candidate-6 Product remediation. It does not authorize Runtime
creation, MariaDB/Redis mutation, Paper start, plugin installation, migration, Minecraft Client
connection, PR merge/Ready transition, tag, Release, Project Issue #4 change, Fixture change, or
`requirements_cleared`.

```text
CANDIDATE-5:
  REJECTED / PRESERVED

CANDIDATE-6:
  REQUIRED

RUNTIME PREFLIGHT:
  DO NOT START

CLIENT TEST:
  DO NOT START
```

## 2. Recovery gate

The remote branch contains documentation commits created after Luna's Candidate-5 stop. Before any
Product edit:

1. fetch origin and obtain current branch, worktree/index status, Local HEAD, Origin HEAD, and PR #14
   HEAD/state;
2. verify the branch is `feature/V0.0.2-main-frontier`;
3. verify Candidate-5 Product HEAD
   `3ba94dd561e2f845fd7726329bd89cdbfb51d51a` is an ancestor of Origin HEAD;
4. verify the Candidate-5 independent review, retired Candidate-5 runtime handoff, and this file are
   present in the Origin tree;
5. when the worktree/index is clean, Local is not ahead or diverged, and Origin HEAD equals PR HEAD,
   fast-forward only with:

```bash
git merge --ff-only origin/feature/V0.0.2-main-frontier
```

Stop without reset, clean, stash, rebase, amend, cherry-pick, force push, or deletion when the gate
is not cleanly satisfied.

Before changing tests or Product code, report:

```text
LOCAL HEAD
ORIGIN HEAD
PR HEAD
FAST-FORWARD: PERFORMED / NOT NEEDED / BLOCKED
WORKTREE/INDEX: CLEAN / DIRTY
PR #14: OPEN / DRAFT / UNMERGED or mismatch
CANDIDATE-5 ARTIFACTS: PRESERVED / NOT VERIFIED
RUNTIME AND CLIENT TEST: NOT STARTED / mismatch
NEXT ACTION
```

## 3. Authorized scope

Candidate-6 is limited to:

1. close the asynchronous Reissue/Revoke/refresh Held Authorization ordering defect;
2. restore exact current authorization after success, conflict, no-change, or failure;
3. add representative Main tests for the changed handler families and managed-action boundary;
4. add Frontier runtime-level tests for late-MVI cancellation on quit, actual world leave, newer
   external entry, and plugin/runtime stop;
5. change Frontier Product code only when those tests expose a real defect;
6. preserve every accepted Candidate-5 behavior;
7. update only tracked status/contracts made false by Candidate-6.

No balance, migration, permission, ordinary inventory-ownership, Resource Pack, Fixture, or unrelated
refactor is authorized.

## 4. Tests first

Add focused tests before Product changes and preserve intended RED evidence for the confirmed async
ordering defect.

### 4.1 Required async-authority tests

Tests must control or hold the database stage incomplete and prove:

- calling Reissue/Revoke makes the old cached authorization unusable before the database mutation can
  complete;
- calling a refresh whose purpose is to observe potentially changed authority makes the old cache
  unusable before the authoritative read can complete;
- a successful mutation installs and authorizes the new Instance/Epoch/status;
- a `NO_CHANGE` result restores/retains correct current authorization;
- a replace conflict reloads the winning authoritative row and does not retain the losing old cache;
- a database failure restores authorization from the actual current authority when available, or
  remains safely fail-closed when authority cannot be established;
- an older Item Instance/Epoch cache is never restored;
- an online Player is not left permanently unavailable after a recoverable conflict or failure.

The RED run must fail because Candidate-5 invalidates after the database work rather than before it,
not because of compilation or test setup failure.

### 4.2 Required Main regression coverage

Use bounded representative or parameterized tests rather than an exhaustive Cartesian product.
Collectively prove:

- held-slot and hand-swap transition fail-close;
- accepted inventory click/number-key and drag transition fail-close;
- Drop/Pickup/Respawn transition fail-close;
- cancelled operations that do not change Main Hand do not leave permanent denial;
- ordinary items remain ordinary after full `NO_MANAGED_ITEM` authorization;
- a stale valid cache cannot authorize a different managed item at Block Break, GUI/interaction,
  Branch, Repair, debug, or Damage boundaries;
- Broken Tool Branch mutation remains denied and accepted Candidate-5 behavior remains green.

A single test may cover multiple related handler families when the assertions remain explicit.

### 4.3 Required Frontier proof

Add runtime-level tests around the actual `FrontierGameplayRuntime` coordination proving that a
pending late-MVI restart is cancelled or rendered obsolete by:

- Player quit;
- actual Frontier world leave;
- a newer external entry cycle; and
- plugin/runtime stop.

Retain proof for one next-tick restart, duplicate coalescing, same-cycle reuse, no unbounded retry,
and the native path when MVI is absent/disabled. Registry-only assertions are insufficient.

Do not modify Frontier Product code when the new tests pass against the existing implementation.

## 5. Product invariant

For Reissue, Revoke, and refresh that can expose changed authority:

1. on the main thread, make the old cached authorization unusable before dispatching asynchronous
   database work;
2. perform no synchronous database or Redis access in Bukkit handlers;
3. on successful completion, install the exact authoritative Session state and fully authorize the
   actual Main-Hand item;
4. on conflict, no-change, cancellation, or failure, read or retain the actual still-authoritative
   state and fully reauthorize it before completing the recoverable operation;
5. when authority cannot be established, remain fail-closed and return a truthful unavailable result;
6. do not expose an authorization generated for an older Item Instance or Epoch.

The implementation structure is not prescribed. Reuse existing task and Session abstractions where
safe, and avoid unrelated architectural changes.

## 6. Validation

Stop at the first failure:

1. focused RED evidence;
2. focused async-authority tests;
3. focused Main handler/action regressions;
4. focused Frontier late-MVI cancellation tests;
5. full Main module tests;
6. full Frontier module tests;
7. repository `check`;
8. `clean assemble`;
9. currently required release/package validators;
10. `git diff --check` and changed-file/scope review.

Record exact commands/results, Java and Gradle identity, test totals, failure/error totals, and skipped
totals. Do not skip, disable, quarantine, or weaken tests to obtain green.

## 7. Product commit and workflow evidence

After local PASS:

- create a clearly identified Candidate-6 Product commit containing Product code, tests, and only
  defining contract updates;
- push normally by fast-forward;
- record exact Candidate-6 Product HEAD;
- monitor the new Normal CI and Pre-client Headless Runtime to completion;
- record event/head SHA, actual checkout SHA, PR merge-ref SHA when used, relation to Candidate-6
  Product HEAD, and conclusion.

A merge-ref success is PR merge-ref evidence, not a direct Product-HEAD checkout. Stop on any failed
or unexplained workflow.

## 8. Candidate-6 fixation

After CI and Headless PASS, perform two independently recorded clean builds from exact Candidate-6
Product HEAD. Each requires a proven clean checkout, exact command, Java/Gradle identity, start/end
time, and Main/Frontier filename, size, SHA-256, and binary comparison.

Both Main builds and both Frontier builds must be byte-identical before fixation.

Core remains the published V0.0.1 authority and the approved Fixture remains unchanged.

Use a new local staging root:

```text
.ai-work/luna-gpt-5.6-v003/candidate/V0.0.2-Client-Candidate-6/
```

Never overwrite Candidate-5. A Product code/resource change after Candidate-6 fixation rejects it and
requires Candidate-7. Metadata or package corrections that do not change fixed Product bytes do not
by themselves require Candidate-7.

## 9. Evidence package and review handoff

Prepare a complete sanitized Candidate-6 submission ZIP and external sidecar containing:

- result report and Candidate-5 review acknowledgement;
- RED and green evidence;
- changed-file list/stat/patch;
- CI/Headless SHA classification;
- two-build evidence;
- Candidate-6 manifest and artifact checksums;
- final Git/PR state;
- a new runtime handoff marked `NOT_STARTED`;
- explicit placeholders for Runtime/Client evidence not authorized in this task.

Do not include JARs, worlds, DB/Redis data, unsanitized full logs, secrets, credentials, or raw Player
identifiers in the review ZIP.

Validate ZIP integrity, complete internal SHA-256 coverage, every referenced file, and the external
sidecar against the final ZIP bytes. Report the exact local package path, filename, size, and SHA-256.
The actual ZIP and sidecar bytes must be supplied to the independent reviewer before Runtime
Preflight is authorized.

## 10. Final state

Successful execution stops with:

```text
PHASE 10C-A CANDIDATE-6 PRODUCT REMEDIATION:
  PASS

CANDIDATE-5:
  REJECTED / PRESERVED

CANDIDATE-6:
  FIXED / PENDING INDEPENDENT REVIEW

RUNTIME PREFLIGHT:
  NOT STARTED

CLIENT TEST:
  NOT STARTED

FULL CLIENT ACCEPTANCE:
  NOT COMPLETE

PRODUCTION BALANCE PROMOTION:
  HOLD

PROJECT ACCEPTANCE:
  PENDING

STABLE PUBLICATION:
  NOT AUTHORIZED
```

PR #14 must remain Open, Draft, and Unmerged. Do not create Candidate-6 Runtime authority values until
the independent Product/package review passes.
