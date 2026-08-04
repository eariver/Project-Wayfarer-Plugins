# Phase 10C-A Candidate-4 Pre-client Independent Review

Recorded: 2026-08-03 JST

## 1. Authority reviewed

```text
Repository:
  eariver/Project-Wayfarer-Plugins

Branch:
  feature/V0.0.2-main-frontier

Candidate-4 Product HEAD:
  9fe86d2e787ab1f86dcf38a5abdba6168515a802

Prepared metadata HEAD initially reviewed:
  9aa5f02d63406890302f6fde485769ac909e9fa4

PR #14:
  Open / Draft / Unmerged / Mergeable

Client Test:
  NOT STARTED
```

The successful Normal CI and Pre-client Headless Runtime runs remain valid evidence for the tested
PR merge-ref trees. They do not override a later source-review blocker.

## 2. Confirmed blocking defect — Broken Tool branch mutation

The Candidate-4 authorization model grants Branch mutation to `VALID_BROKEN_OWNER`.

Relevant source:

```text
plugins/wayfarer-main/src/main/java/io/github/eariver/wayfarer/main/gameplay/
  HeldGrowthToolAuthorization.java
  MainGameplayRuntime.java

plugins/wayfarer-main/src/main/java/io/github/eariver/wayfarer/main/domain/
  GrowthTool.java
```

Confirmed control path:

1. `VALID_BROKEN_OWNER` reports `allowsBranchMutation() == true`.
2. `MainGameplayRuntime.switchBranch(...)` accepts that cached authorization.
3. `GrowthTool.withBranch(...)` changes Branch while retaining `BROKEN` authority status.
4. `switchBranch(...)` then calls `applyEvolution(...)` on the held physical item.
5. The physical rewrite can produce active Growth Tool presentation/state while authority remains
   `BROKEN`.
6. The next physical-claim comparison can therefore report `WRONG_ITEM_STATE` or an equivalent
   authority/physical mismatch.

The accepted contract gives a Broken owner GUI and Repair entry. It does not authorize Branch
mutation while Broken.

Required correction:

```text
VALID_BROKEN_OWNER:
  allowsGui = true
  allowsRepair = true
  allowsBranchMutation = false
```

Required regression evidence:

- Broken Tool Branch mutation is rejected.
- No DB/session Branch mutation occurs.
- The physical item remains `GRAY_DYE` / `BROKEN_GROWTH_TOOL`.
- Tool ID, Item Instance, Epoch, Progress, and Delivery state remain unchanged.
- Active owner Branch mutation continues to work.

This is a Product Code change after Candidate-4 artifact fixation.

## 3. Confirmed blocking defect — stale Held Authorization transition window

The Candidate-4 Main runtime stores authorization by Player UUID and performs full authorization in
`authorizeMainHand(Player)`. Inventory and held-item mutation handlers schedule that method on the
next main-thread turn.

Confirmed source behavior:

- `scheduleAuthorization(Player)` only schedules `authorizeMainHand(...)`.
- It does not remove or replace the old cached result first.
- `onHeldSlot(...)` and `onSwapHands(...)` likewise schedule next-tick authorization without
  synchronous invalidation.
- accepted `onInventoryClick(...)`, `onInventoryDrag(...)`, Drop, Pickup, and Respawn paths leave the
  prior cache observable until the scheduled task runs.
- `guardManagedBreak(...)` checks the actual current item with `wayfarerTool(item)`, but uses the old
  cached `allowsBlockBreak()` result.

Confirmed unsafe transitions:

```text
Case A:
  old Main Hand = valid current Tool
  cache = VALID_ACTIVE_OWNER
  new Main Hand = stale / non-owner / old-epoch managed Tool
  before next-tick authorization = Block Break guard can observe VALID_ACTIVE_OWNER

Case B:
  old Main Hand = ordinary item
  cache = NO_MANAGED_ITEM
  new Main Hand = any managed Tool
  before next-tick authorization = NO_MANAGED_ITEM allows ordinary Block Break
```

The same stale cache can affect GUI, Repair, Branch, debug, Damage handling, and Progress checks that
consult cached capabilities before the next full authorization.

Required correction:

1. every event capable of changing Main Hand must synchronously replace the old authorization with a
   fail-closed transition state before scheduling the next-tick full comparison;
2. do not represent a managed transition as `NO_MANAGED_ITEM`;
3. the Block Break guard must permit a managed item only for an authorization produced for the
   current held physical item and current authority;
4. Reissue, Revoke, authority refresh, status transition, and identity rewrite must invalidate the
   old cache before exposing the changed authority/item to later actions.

The implementation may use `AUTHORITY_UNAVAILABLE` as the transition state, or add a distinct
fail-closed state, provided all managed capabilities are denied and ordinary non-managed behavior is
not blocked after full reauthorization.

Required tests:

- valid current Tool -> stale Tool held-slot transition;
- ordinary item -> stale/non-owner managed Tool transition;
- swap-hand transition;
- number-key/inventory-click transition;
- drag/drop/pickup transitions that alter Main Hand;
- no Block Break, Progress, GUI, Repair, Branch, debug, or managed Damage action during transition;
- valid current Tool becomes usable only after full authorization completes;
- ordinary item remains ordinary after full authorization returns `NO_MANAGED_ITEM`.

This is a confirmed source-level Product defect. It has not been reproduced through a Minecraft
Client because Candidate-4 was stopped before Client Test.

## 4. Frontier late-MVI runtime-test gap

The current `MviReadinessTest` proves `EntryCycleRegistry` state consumption, but does not prove the
complete `FrontierGameplayRuntime` coordination required by the work order.

Missing automated proof:

- TIMEOUT followed by a concrete public MVI MONITOR event schedules exactly one next-tick restart;
- duplicate concrete MVI events coalesce;
- only one late restart can be consumed per external entry cycle;
- quit, actual world leave, newer external entry, and plugin stop cancel the pending restart;
- the restart reuses the external cycle rather than recursively starting a new one;
- no unbounded retry path exists.

Candidate-5 must add a focused runtime-state test or extract a pure coordination component that
proves these transitions. A Registry-only test is insufficient because the real-client plan does
not intentionally force this branch.

## 5. Frontier timeout-diagnostic gap

Candidate-3 could not be localized because poll-by-poll visibility evidence was absent. Candidate-4
increased the finite window, but its final TIMEOUT line still omits the complete terminal state
required by the Revision B instruction.

The final bounded TIMEOUT evidence must contain:

```text
source
generation
pollCount
visibleManagedItems
requiredManagedItems
fingerprint
decision=TIMEOUT
```

The log must remain sanitized and must not include raw Player UUID. Prefer a pure immutable
observation/snapshot value that can be unit-tested before formatting the log line.

Changing runtime diagnostics changes Product bytes and therefore belongs to Candidate-5.

## 6. Reproducibility and runtime gates

Candidate-4 retained one preliminary build and one formally qualified clean build. The preliminary
build was byte-identical but lacked the original durable start/end record and was explicitly not
accepted as Formal Build 1.

Candidate-5 must perform two fully qualified clean builds from the exact Candidate-5 Product HEAD.
Both Main and Frontier JARs must match by size and SHA-256 before fixation.

Fresh runtime creation, Plugin Enable, Migration, and real backend preflight were not executed for
Candidate-4. Candidate-5 must complete authorized disposable runtime preparation outside Project
Runtime before any Minecraft Client connection.

## 7. Local-only evidence boundary

The Candidate-4 manifest, checksum file, reproducibility records, runtime-preflight record, and
worksheets were recorded as ignored local files. They were not independently reviewed through a
complete Candidate-4 submission package.

Candidate-5 must produce the complete sanitized submission ZIP and external sidecar required by the
formal instruction. Every required placeholder path must exist even when a gate stops or waits.

## 8. Independent verdict

```text
PHASE 10C-A PRE-CLIENT INDEPENDENT REVIEW:
  FAIL / HOLD

CANDIDATE-4:
  REJECTED BEFORE CLIENT TEST

CONFIRMED PRODUCT DEFECT 1:
  BROKEN TOOL BRANCH MUTATION ALLOWED

CONFIRMED PRODUCT DEFECT 2:
  STALE HELD AUTHORIZATION TRANSITION WINDOW

FRONTIER LATE-MVI RUNTIME TEST:
  INSUFFICIENT

FRONTIER TIMEOUT DIAGNOSTICS:
  INSUFFICIENT

FORMAL REPRODUCIBLE BUILDS:
  INCOMPLETE FOR A TWO-FORMAL-BUILD GATE

FRESH RUNTIME PREFLIGHT:
  NOT EXECUTED

CANDIDATE-5:
  REQUIRED

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

## 9. Required Candidate-5 remediation scope

Candidate-5 must remain narrowly scoped to:

1. deny Branch mutation for `VALID_BROKEN_OWNER` and add regressions;
2. synchronously fail-close Held Authorization at every Main-Hand-changing boundary and add
   transition tests;
3. add real late-MVI restart coordination tests;
4. add complete bounded TIMEOUT diagnostic state;
5. rerun focused Main/Frontier tests, module tests, `check`, and `assemble`;
6. create a new Product commit without modifying Candidate-4 artifacts;
7. obtain successful CI and Headless evidence for Candidate-5;
8. perform two fully qualified clean reproducible builds;
9. fix new Candidate-5 artifacts and hashes;
10. prepare a fresh Candidate-5 runtime authority and complete server-side preflight;
11. stop immediately before the first Minecraft Client action for a new independent review.

Do not reuse the Candidate-4 label or overwrite Candidate-4 fixed JARs.

## 10. Explicit non-actions

This independent review does not:

- modify Product Code or Tests;
- modify Candidate-4 artifacts;
- start MariaDB, Redis, Main, Frontier, or a Minecraft Client;
- modify Project Runtime or Project Issue #4;
- merge or mark PR #14 Ready;
- create a tag or Release;
- assert `requirements_cleared`.
