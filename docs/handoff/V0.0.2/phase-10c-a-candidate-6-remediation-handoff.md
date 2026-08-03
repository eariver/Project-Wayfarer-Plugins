# Phase 10C-A Candidate-6 Remediation Handoff

Revision: C  
Recorded: 2026-08-03 JST

## 1. Purpose and authority

Candidate-5 was rejected before Runtime Preflight by:

`docs/release-readiness/V0.0.2/phase-10c-a-candidate-5-independent-review.md`

This handoff authorizes one narrow Candidate-6 Product remediation.

Owner clarification on 2026-08-03:

- only Minecraft Client connection and client-driven scenarios are deferred;
- MariaDB/Redis connectivity, integration tests, migrations in disposable test environments,
  GitHub Actions service containers, Headless Paper, plugin enable/migration verification, and
  server-side preflight are not prohibited merely because they use DB/Redis/Paper;
- repository/runtime ownership boundaries still apply: do not mutate production or unrelated Project
  Runtime state from an unauthorized context.

The current Product task may therefore run all non-client validation needed to fix, verify, build,
classify, and package Candidate-6. After independent Product/package review passes, a separately
runtime-authorized task may complete fresh server-side Runtime Preflight and must stop immediately
before the first Minecraft Client connection.

This task does not authorize Minecraft Client connection, client-driven test scenarios, PR
merge/Ready transition, tag, Release, Project Issue #4 change, Fixture change, or
`requirements_cleared`.

```text
CANDIDATE-5:
  REJECTED / PRESERVED

CANDIDATE-6:
  REQUIRED

NON-CLIENT VALIDATION:
  AUTHORIZED

SERVER-SIDE RUNTIME PREFLIGHT:
  AUTHORIZED AFTER INDEPENDENT PRODUCT/PACKAGE REVIEW

MINECRAFT CLIENT TEST:
  DEFERRED / DO NOT START
```

## 2. Recovery gate

The remote branch contains documentation commits created after Luna's Candidate-5 stop. Before any
Product edit:

1. fetch origin and obtain current branch, worktree/index status, Local HEAD, Origin HEAD, and PR #14
   HEAD/state;
2. verify the branch is `feature/V0.0.2-main-frontier`;
3. verify Candidate-5 Product HEAD
   `3ba94dd561e2f845fd7726329bd89cdbfb51d51a` is an ancestor of Origin HEAD;
4. verify the Candidate-5 independent review, retired Candidate-5 runtime handoff, Candidate-5 package
   audit, and this file are present in the Origin tree;
5. when the worktree/index is clean, Local is not ahead or diverged, and Origin HEAD equals PR HEAD,
   fast-forward only with:

```bash
git merge --ff-only origin/feature/V0.0.2-main-frontier
```

Stop without reset, clean, stash, rebase, amend, cherry-pick, force push, or deletion when the gate
is not cleanly satisfied.

Before changing tests or Product code, report only:

```text
LOCAL HEAD
ORIGIN HEAD
PR HEAD
FAST-FORWARD: PERFORMED / NOT NEEDED / BLOCKED
WORKTREE/INDEX: CLEAN / DIRTY
PR #14: OPEN / DRAFT / UNMERGED or mismatch
CANDIDATE-5 ARTIFACTS: PRESERVED / NOT VERIFIED
MINECRAFT CLIENT TEST: NOT STARTED / mismatch
NEXT ACTION
```

## 3. Authorized scope

Candidate-6 is limited to:

1. close the asynchronous Reissue/Revoke/refresh Held Authorization ordering defect;
2. restore exact current authorization after success, conflict, no-change, or failure;
3. add the minimum Main tests needed to prove that ordering and any still-uncovered changed handler
   family or managed-action invariant;
4. add Frontier runtime-level tests for late-MVI cancellation on quit, actual world leave, newer
   external entry, and plugin/runtime stop;
5. change Frontier Product code only when those tests expose a real defect;
6. preserve every accepted Candidate-5 behavior;
7. update only tracked status/contracts made false by Candidate-6.

No balance, migration definition, permission, ordinary inventory-ownership, Resource Pack, Fixture,
or unrelated refactor is authorized.

Database migrations may be executed against disposable validation schemas when required by existing
integration or pre-client workflows. Do not alter migration definitions unless a Candidate-6 Product
defect actually requires it.

## 4. Tests first

Add focused tests before Product changes and preserve one intended RED run for the confirmed async
ordering defect. Do not manufacture RED evidence for Frontier cancellation when the existing Product
already behaves correctly.

### 4.1 Required async-authority proof

Tests must control or hold the database stage incomplete and prove:

- Reissue/Revoke makes the old cached authorization unusable before the database mutation can
  complete;
- refresh makes the old cache unusable before the authoritative read can complete when refresh is
  intended to observe potentially changed authority;
- success installs and authorizes the new Instance/Epoch/status;
- `NO_CHANGE` restores or retains the correct current authorization;
- conflict reloads the winning authoritative row and does not retain the losing old cache;
- database failure restores authorization from the actual current authority when available, or
  remains safely fail-closed when authority cannot be established;
- an older Item Instance/Epoch cache is never restored;
- an online Player is not left permanently unavailable after a recoverable conflict or failure.

The RED run must fail on the Candidate-5 ordering behavior, not on compilation or fixture setup.

### 4.2 Main regression coverage

Existing Candidate-5 tests may satisfy accepted behavior. Add only the missing representative tests
needed so that the combined suite covers:

- held-slot and hand-swap transition fail-close;
- accepted inventory click/number-key and drag transition fail-close;
- Drop/Pickup/Respawn transition fail-close;
- cancelled operations that do not change Main Hand do not leave permanent denial;
- ordinary items remain ordinary after full `NO_MANAGED_ITEM` authorization;
- stale valid cache cannot authorize a different managed item at the distinct Block Break,
  GUI/interaction, Branch, Repair, debug, or Damage boundaries;
- Broken Tool Branch mutation remains denied.

Do not create an event-by-capability Cartesian test matrix. One test may prove multiple closely
related handler families when the assertions are explicit.

### 4.3 Frontier proof

Add focused runtime-level tests around the actual `FrontierGameplayRuntime` coordination proving that
a pending late-MVI restart is cancelled or rendered obsolete by:

- Player quit;
- actual Frontier world leave;
- a newer external entry cycle; and
- plugin/runtime stop.

Existing Candidate-5 tests continue to provide the positive restart, duplicate coalescing, same-cycle
reuse, bounded retry, and native-no-MVI evidence. Do not duplicate those tests unless the new test
fixture requires a small consolidation. Registry-only assertions are insufficient.

Do not modify Frontier Product code when the new tests pass against the existing implementation.

## 5. Product invariant

For Reissue, Revoke, and refresh that can expose changed authority:

1. on the main thread, make the old cached authorization unusable before dispatching asynchronous
   database work;
2. perform no synchronous database or Redis access in Bukkit handlers;
3. on success, install the exact authoritative Session state and fully authorize the actual Main-Hand
   item;
4. on conflict, no-change, cancellation, or recoverable failure, read or retain the actual
   still-authoritative state and fully reauthorize it;
5. when authority cannot be established, remain fail-closed and return a truthful unavailable result;
6. never expose an authorization generated for an older Item Instance or Epoch.

The implementation structure is not prescribed. Reuse existing task and Session abstractions where
safe and avoid unrelated architectural changes.

## 6. Validation and Product fixation

Stop at the first failure:

1. focused RED evidence for the async defect;
2. focused async-authority tests;
3. only the missing Main handler/action regressions;
4. focused Frontier cancellation tests;
5. full Main module tests;
6. full Frontier module tests;
7. MariaDB/Redis-backed integration tests required by the repository validation profile;
8. repository `check`;
9. `clean assemble`;
10. release/package validators applicable to Candidate-6;
11. `git diff --check` and changed-file/scope review.

GitHub Actions may start disposable MariaDB/Redis service containers and Headless Paper. Local or
separate test workspaces may also start disposable MariaDB/Redis/Paper when required for authorized
non-client validation. These are not Minecraft Client tests and are not prohibited.

Record commands/results, Java and Gradle identity, test totals, failures/errors, and skipped totals. Do
not skip, disable, quarantine, or weaken tests to obtain green.

After local PASS:

- create a clearly identified Candidate-6 Product commit;
- push normally by fast-forward;
- record exact Candidate-6 Product HEAD;
- monitor Normal CI and Pre-client Headless Runtime to completion;
- record event/head SHA, checkout SHA, PR merge-ref SHA when used, relation to Product HEAD, and
  conclusion.

After CI and Headless PASS, perform two independently recorded clean builds from exact Candidate-6
Product HEAD. Each requires a clean checkout, exact command, Java/Gradle identity, and Main/Frontier
filename, size, SHA-256, and binary comparison. A timestamp or run identifier sufficient to
distinguish the two builds is required; exact start/end timestamps are not a separate Gate.

Both Main builds and both Frontier builds must be byte-identical before fixation.

Core remains the published V0.0.1 authority and the approved Fixture remains unchanged.

Use a new local staging root:

```text
.ai-work/luna-gpt-5.6-v003/candidate/V0.0.2-Client-Candidate-6/
```

Never overwrite Candidate-5. A Product code/resource change after Candidate-6 fixation rejects it and
requires Candidate-7. Metadata or package corrections that do not change fixed Product bytes do not
by themselves require Candidate-7.

## 7. Evidence package and runtime handoff

Prepare a complete sanitized Candidate-6 submission ZIP and external sidecar containing:

- Candidate-6 manifest and artifact checksums;
- result report and Candidate-5 review/package-audit acknowledgement;
- focused RED evidence and concise green command/result records;
- changed-file list and change summary with the exact Product commit/range;
- CI/Headless SHA classification;
- two-build evidence;
- final Git/PR state;
- a server-side runtime handoff marked `PENDING INDEPENDENT REVIEW`;
- explicit Minecraft Client Test `NOT_STARTED` records.

A full repository patch, complete raw test log, or duplicate copy of tracked source is not required
because the exact Product commit is independently accessible. Include a patch only when it is needed
to explain an untracked/local-only change, which should normally not exist.

Do not include JARs, worlds, DB/Redis data, unsanitized logs, secrets, credentials, or raw Player
identifiers in the review ZIP.

Validate ZIP integrity, complete internal SHA-256 coverage, every referenced file, and the external
sidecar against the final ZIP bytes. Report the exact local package path, filename, size, and SHA-256.
The actual ZIP and sidecar bytes must be supplied to the independent reviewer before fresh
server-side Runtime Preflight begins.

The runtime handoff may reserve proposed Candidate-6-specific schema/prefix/server IDs and ports for
later review. After independent Product/package review passes, those values may be created and used in
a fresh isolated runtime task without waiting for Minecraft Client availability. That task must
complete all server-side checks and stop before the first Minecraft Client connection.

## 8. Final state

Successful Product/package execution stops with:

```text
PHASE 10C-A CANDIDATE-6 PRODUCT REMEDIATION:
  PASS

CANDIDATE-5:
  REJECTED / PRESERVED

CANDIDATE-6:
  FIXED / PENDING INDEPENDENT REVIEW

NON-CLIENT CI / INTEGRATION / HEADLESS VALIDATION:
  COMPLETE

SERVER-SIDE RUNTIME PREFLIGHT:
  PENDING INDEPENDENT PRODUCT/PACKAGE REVIEW

MINECRAFT CLIENT TEST:
  NOT STARTED / DEFERRED

FULL CLIENT ACCEPTANCE:
  NOT COMPLETE

PRODUCTION BALANCE PROMOTION:
  HOLD

PROJECT ACCEPTANCE:
  PENDING

STABLE PUBLICATION:
  NOT AUTHORIZED
```

PR #14 must remain Open, Draft, and Unmerged.
