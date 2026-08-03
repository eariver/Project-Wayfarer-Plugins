# Phase 10C-A Candidate-7 Remediation Handoff

Revision: A  
Recorded: 2026-08-03 JST

## 1. Purpose and boundary

Candidate-6 was rejected before Server-side Runtime Preflight by:

`docs/release-readiness/V0.0.2/phase-10c-a-candidate-6-independent-product-package-review.md`

This handoff authorizes one narrow Candidate-7 Product remediation for stale same-Player async
authority completions. It does not authorize unrelated Product work, PR merge/Ready transition, tag,
Release, Project Issue #4 change, Fixture change, `requirements_cleared`, Minecraft Client connection,
or client-driven scenarios.

Non-client validation remains authorized, including disposable MariaDB/Redis integration, migrations
in disposable test environments, GitHub Actions service containers, Headless Paper, and package
validation.

```text
CANDIDATE-6:
  REJECTED / PRESERVED

CANDIDATE-7:
  REQUIRED

SERVER-SIDE RUNTIME PREFLIGHT:
  DO NOT START BEFORE INDEPENDENT CANDIDATE-7 REVIEW

MINECRAFT CLIENT TEST:
  DEFERRED / DO NOT START
```

## 2. Recovery gate

The remote branch contains review/handoff commits created after Luna's Candidate-6 stop. Before any
Product edit:

1. fetch Origin and report current branch, clean/dirty state, Local HEAD, Origin HEAD, and PR #14
   HEAD/state;
2. verify the branch is `feature/V0.0.2-main-frontier`;
3. verify Candidate-6 Product HEAD
   `2a3f1cc384c397e610aba33c6ffc0f6a29af2987` and synchronization merge
   `b0bb5a5f2047a87a5313701d5ae46f825aec16d4` are ancestors of Origin HEAD;
4. verify the Candidate-6 independent review and this handoff are present in the Origin tree;
5. verify Candidate-6 artifacts and submission ZIP/sidecar are preserved;
6. when Worktree/Index are clean, Local is not ahead or diverged, and Origin HEAD equals PR HEAD,
   update only by safe fast-forward:

```bash
git merge --ff-only origin/feature/V0.0.2-main-frontier
```

Do not use reset, clean, stash, rebase, amend, cherry-pick, or force push. Stop and report when the
gate is not cleanly satisfied.

Before changing tests or Product code, report only:

```text
LOCAL HEAD
ORIGIN HEAD
PR HEAD
FAST-FORWARD: PERFORMED / NOT NEEDED / BLOCKED
WORKTREE/INDEX: CLEAN / DIRTY
PR #14: OPEN / DRAFT / UNMERGED or mismatch
CANDIDATE-6 ARTIFACTS / PACKAGE: PRESERVED / NOT VERIFIED
SERVER-SIDE RUNTIME PREFLIGHT: NOT STARTED / mismatch
MINECRAFT CLIENT TEST: NOT STARTED / mismatch
NEXT ACTION
```

When the gate passes, continue without waiting for another confirmation.

## 3. Authorized Product scope

Candidate-7 is limited to:

1. preserve immediate main-thread fail-close before Reissue/Revoke/refresh database dispatch;
2. prevent an older same-Player async completion from changing Session, inventory, or Held
   Authorization after a newer request exists or a newer authority has been applied;
3. make quit/stop invalidate or obsolete outstanding same-Player authority completions;
4. add the minimum deterministic tests needed to prove reverse completion ordering;
5. preserve all accepted Candidate-6 Main action-boundary and Frontier behavior;
6. update only tracked status/contracts made false by Candidate-7.

No balance, migration definition, permission, Fixture, Resource Pack, ordinary inventory ownership,
Frontier Product, or unrelated refactor is authorized unless a required regression test exposes a
direct defect.

## 4. Tests first

Before Product changes, add a focused RED test that fails on Candidate-6 for the confirmed stale
completion defect. The test must fail on the ordering assertion, not compilation or fixture setup.

Deterministically control at least two same-Player asynchronous operations and complete them in an
unsafe reverse order. The combined focused tests must prove:

- an older refresh result containing Epoch N cannot overwrite a later applied Reissue result for
  Epoch N+1;
- an older successful Reissue completion cannot overwrite a later Revoke or later authority result;
- a stale conflict/failure recovery completion cannot overwrite the latest current authority;
- a stale completion performs no Session open, inventory reconciliation, or Held Authorization
  restoration;
- the latest recoverable operation restores the current database authority;
- the first request still fail-closes immediately before its database stage completes;
- quit and runtime stop prevent outstanding completion from reopening Player state.

Use bounded representative or parameterized tests. Do not create an exhaustive operation Cartesian
matrix.

## 5. Product invariant

For each Player, Reissue/Revoke/refresh operations that can expose changed authority must have an
explicit ordering guarantee.

Required behavior:

1. request acceptance invalidates Held Authorization on the main thread before database dispatch;
2. the operation captures a per-Player ordering identity or enters an equivalent serialization
   mechanism;
3. before applying an async result on the main thread, revalidate that the completion is still
   current;
4. stale completions are ignored for Session, inventory, and Held Authorization mutation;
5. the latest operation is responsible for authoritative recovery after conflict/failure;
6. no authorization for an older Item Instance/Epoch is restored or exposed;
7. no synchronous database or Redis access is added to Bukkit handlers.

The implementation structure is not prescribed. A monotonic generation token, per-Player serialized
async chain with immediate invalidation, or another bounded equivalent is acceptable. Avoid a global
lock and unrelated architecture changes.

## 6. Validation and Candidate fixation

Stop at the first failure:

1. focused RED evidence;
2. focused reverse-completion tests;
3. existing Candidate-6 async-authority and Main transition/action tests;
4. Frontier regression tests; no new Frontier Product work is expected;
5. full Main and Frontier module tests;
6. MariaDB/Redis-backed integration tests required by the repository profile;
7. repository `check`;
8. `clean assemble`;
9. applicable release/package validators;
10. `git diff --check` and changed-file/scope review.

Record commands/results, Java and Gradle identity, test totals, failures/errors, and skipped totals.
Do not skip, disable, quarantine, or weaken tests to obtain green.

After local PASS:

- create and normally push a clearly identified Candidate-7 Product commit;
- record exact Candidate-7 Product HEAD;
- monitor Normal CI and Pre-client Headless Runtime to completion;
- classify event/head SHA, checkout SHA, PR merge-ref SHA, relation to Product HEAD, and conclusion;
- perform two independently recorded clean builds from exact Candidate-7 Product HEAD;
- require byte-identical Main and Frontier outputs before fixation;
- stage Candidate-7 separately from Candidate-6.

A Product code/resource change after Candidate-7 fixation rejects Candidate-7 and requires
Candidate-8. Metadata/package corrections that do not change fixed Product bytes do not by themselves
require a new candidate.

## 7. Candidate-7 evidence package

Create a new complete sanitized Candidate-7 ZIP and external sidecar. Candidate-6 submission bytes
must not be overwritten.

The package may reuse the concise Candidate-6 structure and must contain:

- Candidate-7 manifest and artifact checksums;
- Candidate-6 rejection acknowledgement;
- focused RED and green ordering evidence;
- changed-file list and exact Product commit/range;
- CI/Headless SHA classification;
- two-build evidence;
- final Git/PR state;
- Server-side Runtime Handoff marked pending independent review;
- Minecraft Client `NOT_STARTED / DEFERRED` record;
- internal `SHA256SUMS.txt` covering every other entry exactly once.

A complete repository patch, raw logs, tracked-source copies, JARs, worlds, DB/Redis data, runtime
configuration, credentials, secrets, and raw Player identifiers are not required and must not be
included.

Supply the actual ZIP and sidecar bytes for independent review before Server-side Runtime Preflight.

## 8. Successful stop state

```text
PHASE 10C-A CANDIDATE-7 PRODUCT REMEDIATION:
  PASS

CANDIDATE-6:
  REJECTED / PRESERVED

CANDIDATE-7:
  FIXED / PENDING INDEPENDENT REVIEW

NON-CLIENT VALIDATION:
  COMPLETE

SERVER-SIDE RUNTIME PREFLIGHT:
  NOT STARTED / PENDING INDEPENDENT REVIEW

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