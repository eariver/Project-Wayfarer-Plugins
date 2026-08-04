# Phase 10C-A Candidate-5 Remediation Handoff

Revision: B  
Recorded: 2026-08-03 JST

## 1. Purpose and boundary

Candidate-4 was rejected before Client Test. Candidate-5 is a narrow Product remediation for the
confirmed Main defects and the unresolved Frontier proof/diagnostic gaps.

This task may modify Product code, tests, and the minimum tracked contracts needed to describe those
changes. It must not start or modify Project Runtime, connect a Minecraft Client, merge PR #14, mark
it Ready, create a tag or Release, modify Project Issue #4, change the approved Fixture, or set
`requirements_cleared`.

Successful execution stops after Product validation, CI/Headless evidence, two formal clean builds,
Candidate-5 fixation, a complete sanitized review package, truthful tracked status, and a separate
runtime handoff.

## 2. Starting gate

Start through:

`docs/handoff/V0.0.2/phase-10c-a-candidate-5-execution-entry.md`

Required state after its recovery gate:

```text
Repository: eariver/Project-Wayfarer-Plugins
Branch: feature/V0.0.2-main-frontier
Local HEAD = Origin HEAD = PR #14 HEAD
Worktree/index: clean
PR #14: Open / Draft / Unmerged
Candidate-4 Product HEAD: 9fe86d2e787ab1f86dcf38a5abdba6168515a802
Candidate-4 artifacts: preserved and immutable
Client Test: not started
```

Verify that no V0.0.2 tag or Release exists. Stop without destructive Git actions when any starting
condition is false.

## 3. Authorized scope

Candidate-5 is limited to:

1. deny Branch mutation for a Broken Growth Tool;
2. prevent a cached Held Authorization from authorizing a different or newly changed Main-Hand item;
3. require actual managed-item presence at managed action entry points without restoring per-action
   full PDC/DB authorization;
4. prove the Frontier late-MVI restart behavior at runtime-coordination level;
5. add complete bounded Frontier TIMEOUT diagnostics;
6. add the tests and minimum contract/status updates required by those changes.

Do not alter balance, migrations, permissions, Fixture behavior, Resource Pack handling, unrelated
code, or ordinary inventory ownership.

## 4. Tests first

Add focused tests before Product changes and preserve a RED run for each defect group:

- Broken Tool Branch mutation;
- Held Authorization transition/managed-action boundary;
- Frontier late-MVI coordination and TIMEOUT diagnostics.

The current pre-fix branch is an acceptable RED baseline because its Product tree still contains the
Candidate-4 behavior. Do not check out, revert, or rewrite Candidate-4 solely to manufacture RED
evidence.

RED evidence needs the command, expected assertion, actual intended failure, and confirmation that
the failure is not a compilation or test-setup error.

## 5. Main acceptance criteria

### 5.1 Broken Tool capability

`VALID_BROKEN_OWNER` may open the GUI and enter Repair, but must not mutate Branch or use debug paths
that assume an active Tool. `VALID_ACTIVE_OWNER` retains the authorized Branch behavior.

Tests must show that rejecting a Broken Branch mutation leaves authority, Branch, progress, Tool ID,
Item Instance, Epoch, delivery state, and the `GRAY_DYE` / `BROKEN_GROWTH_TOOL` physical state
unchanged.

### 5.2 Held Authorization transition invariant

A cached authorization is valid only for the Main-Hand/authority state for which it was produced.
Whenever a handler or asynchronous completion can expose a changed Main-Hand item, Item Instance,
Epoch, status, or authority, the old cache must become unusable before a later managed action can
observe the change.

The implementation may either:

- install a synchronous fail-closed transition state and perform full authorization afterward; or
- complete full authorization synchronously in the same uninterrupted main-thread operation when
  the final state is already available.

Do not add synchronous DB/Redis access to Bukkit handlers. Do not reparse the complete claim on every
use.

Cover each Main-Hand-changing handler family that exists in Product code, including held-slot,
hand-swap, accepted inventory mutation/number-key, drag, Drop, Pickup, and Respawn. Cancelled
operations whose Main Hand did not change must not leave the Player permanently fail-closed.

For Reissue, Revoke, refresh, break, repair, and identity rewrite, preserve this invariant without
forcing an unnecessary new state transition when the existing main-thread path already installs the
final authority/item and reauthorizes it before returning.

On asynchronous conflict or failure, restore authorization for the still-authoritative state; do not
leave a Player permanently unavailable and do not restore a cache from an older Instance or Epoch.

### 5.3 Managed action boundary

Use handlers may perform a lightweight check that the actual Main-Hand item is Wayfarer-managed, then
consult the cache. They must not authorize an ordinary or different managed item from a stale cache.

Required behavior:

```text
Actual item is ordinary:
  ordinary Minecraft behavior; Wayfarer does not interfere

Actual item is managed and cached state is invalid/transition:
  deny the managed operation

Actual item is managed and cached state is the exact allowed state:
  permit only that capability
```

In particular:

- Block Break for an actual managed item requires exactly `VALID_ACTIVE_OWNER`; otherwise cancel it.
- Progress remains a non-cancelling MONITOR action and runs only after a successful uncancelled break
  with `VALID_ACTIVE_OWNER`.
- Right-click with an invalid managed item is cancelled and opens no GUI.
- Right-click with an ordinary item is untouched.
- Branch and active debug mutation require an actual managed active Tool and
  `VALID_ACTIVE_OWNER`.
- Repair requires an actual managed Tool and the matching active/broken authorization.
- Damage handling must not mutate a different item under a stale valid cache.

Tests may separate pure capability/transition tests from a smaller set of runtime-handler tests; an
exhaustive Cartesian product of every event and every capability is not required. Every production
handler family and every security-relevant capability must nevertheless be represented.

## 6. Frontier acceptance criteria

### 6.1 Late-MVI restart

Automated tests must prove the real coordination used by `FrontierGameplayRuntime`, not only an
isolated registry flag:

- a TIMEOUT followed by a qualifying public MVI MONITOR event schedules one next-tick restart;
- duplicate events coalesce;
- at most one late restart is consumed per external entry cycle;
- the internal restart reuses that cycle and cannot create an unbounded retry chain;
- quit, actual world leave, a newer external entry, and plugin stop cancel obsolete pending work;
- MVI absent/disabled retains the native Bukkit fingerprint path.

The implementation structure is not prescribed. A small pure coordinator may be extracted when that
is the least invasive way to test the runtime contract.

### 6.2 TIMEOUT evidence

The terminal bounded TIMEOUT observation and sanitized log must contain:

```text
source
generation
pollCount
visibleManagedItems
requiredManagedItems
fingerprint
decision=TIMEOUT
```

Do not log raw Player UUID. Retain the accepted finite readiness constants and do not replace the
readiness algorithm with an unconditional delay.

## 7. Tracked documentation

Update only tracked files whose assertions become false or incomplete because of Candidate-5.
Normally this includes affected requirements/traceability, release-readiness, handoff/status, Client
plan where its steps change, and PR #14.

Do not update a Decision Register or source-provenance document unless Candidate-5 actually creates a
new decision or changes the provenance statement. Avoid broad documentation churn.

All status surfaces must agree that Candidate-4 is rejected/preserved, Candidate-5 is the current
Product candidate, Client Test has not started, Full Client Acceptance is incomplete, promotion is
HOLD, Project Acceptance is pending, and stable publication is not authorized.

## 8. Validation

Stop at the first failure:

1. focused RED evidence;
2. focused Main tests;
3. focused Frontier tests;
4. full Main module tests;
5. full Frontier module tests;
6. repository `check`;
7. `clean assemble`;
8. currently required release/package validators;
9. `git diff --check` and changed-file/scope review.

Record exact commands and results, Java/Gradle identity for the validation session, test totals, and
skipped totals. Per-command timestamps are not required; record start/end time for the formal
reproducible builds and workflows where timing is material.

Do not skip, disable, quarantine, or weaken a test to obtain green.

## 9. Product commit and workflows

After local PASS, create a clearly identified Candidate-5 Product commit containing Product code,
tests, and contracts that define the Product behavior. Push normally and record its SHA.

Later evidence/status-only commits are allowed, but they must not alter Product bytes or redefine the
Candidate-5 Product HEAD.

Monitor the new Normal CI and Pre-client Headless Runtime to completion. For each run record:

- event/head SHA;
- actual checkout SHA;
- PR merge-ref SHA when applicable;
- relation of the tested tree to Candidate-5 Product HEAD;
- conclusion.

Do not describe merge-ref validation as direct Product-HEAD checkout, and do not modify workflows
merely to obtain a preferred evidence label.

## 10. Two formal clean builds and fixation

After Product CI and Headless succeed, perform two independently recorded clean builds from exact
Candidate-5 Product HEAD. Each build must have a proven clean checkout, exact command, Java/Gradle
identity, start/end time, and Main/Frontier filename, size, and SHA-256.

Main and Frontier must match across both builds by filename, size, SHA-256, and bytes before
fixation.

Core remains the published V0.0.1 authority:

```text
Wayfarer_Core-V0.0.1.jar
size: 11751447
sha256: b045581d3984dddba10ed7b2ada435926b8538ba9b29a1151550ce59588395a2
```

The approved Fixture remains unchanged:

```text
docs/testing/fixtures/V0.0.2/leafgrapple-wayfarer-test-only.yml
sha256: ed210f8e56db26315f91fecb9e1d35d686c8fe647480498b7588467a6fa2448a
```

Use a new local staging root:

```text
.ai-work/luna-gpt-5.6-v003/candidate/V0.0.2-Client-Candidate-5/
```

Never overwrite Candidate-4. A Product code/resource change after Candidate-5 fixation rejects
Candidate-5 and requires Candidate-6. A metadata, packaging, or runtime-preparation correction that
does not change fixed Product bytes does not by itself require Candidate-6.

## 11. Evidence package

Prepare local-only Candidate-5 manifest/checksums, change summaries, result report, CI/Headless
evidence, RED/green evidence, two-build evidence, runtime handoff, and final Git/PR state.

Create the complete sanitized submission ZIP and external sidecar required by the formal process.
The ZIP must include explicit `NOT_STARTED` placeholders for runtime/client evidence that this task is
not authorized to create. It must not include JARs, worlds, DB/Redis data, full unsanitized logs,
secrets, or credentials.

Internal `SHA256SUMS.txt` must cover every other archive file exactly once using lowercase SHA-256 and
relative POSIX paths. Validate ZIP integrity, all internal hashes/references, and the external sidecar
against the final ZIP bytes.

## 12. Runtime handoff only

`AGENTS.md` prohibits installation, migration, runtime configuration changes, restart, and deployment
from this Plugin repository context. Do not start MariaDB, Redis, Paper, Main, Frontier, or a
Minecraft Client.

Prepare a separate runtime handoff using fresh Candidate-5 authority:

```text
MariaDB schema: wayfarer_client_v002_c5
Redis prefix: wf-v002-client-c5
Main server ID: wayfarer-client-c5-main
Frontier server ID: wayfarer-client-c5-frontier
Suggested ports: 25572 / 25573, subject to runtime-side availability verification
```

The runtime-authorized task must use only fixed Candidate-5 bytes, create fresh DB/Redis/world/player
and MVI state, perform server-side preflight, and stop before the first Client action for a new
independent review.

## 13. Final gate and stop

Before stopping, verify:

- worktree/index clean;
- local, origin, and PR HEAD agree;
- PR #14 remains Open, Draft, Unmerged;
- no V0.0.2 tag or Release exists;
- PR body and tracked status are truthful;
- Candidate-4 artifacts are unchanged;
- Candidate JARs and prohibited runtime evidence are not tracked.

Successful state:

```text
PHASE 10C-A CANDIDATE-5 PRODUCT REMEDIATION:
  PASS

CANDIDATE-4:
  REJECTED / PRESERVED

CANDIDATE-5:
  FIXED / PREPARED_FOR_RUNTIME_PREFLIGHT

RUNTIME PREFLIGHT:
  NOT STARTED IN PLUGIN REPOSITORY CONTEXT

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
