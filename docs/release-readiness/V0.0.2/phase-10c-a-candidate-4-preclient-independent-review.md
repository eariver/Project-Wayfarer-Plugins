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

Prepared metadata HEAD reviewed:
  9aa5f02d63406890302f6fde485769ac909e9fa4

PR #14:
  Open / Draft / Unmerged / Mergeable

Client Test:
  NOT STARTED
```

The successful Normal CI and Pre-client Headless Runtime runs remain valid evidence for the tested
PR merge-ref trees. They do not override a later source-review blocker.

## 2. Confirmed blocking defect — Broken Tool branch mutation

The Candidate-4 authorization model currently grants branch mutation to
`VALID_BROKEN_OWNER`.

Relevant source:

```text
plugins/wayfarer-main/src/main/java/io/github/eariver/wayfarer/main/gameplay/
  HeldGrowthToolAuthorization.java
  MainGameplayRuntime.java

plugins/wayfarer-main/src/main/java/io/github/eariver/wayfarer/main/domain/
  GrowthTool.java
```

Observed control path:

1. `VALID_BROKEN_OWNER` reports `allowsBranchMutation() == true`.
2. `switchBranch(...)` accepts the cached authorization and calls `GrowthTool.withBranch(...)`.
3. `GrowthTool.withBranch(...)` retains the `BROKEN` authority status.
4. The physical rewrite path applies normal evolution/presentation to the held item.
5. The authority can remain `BROKEN` while the physical item is rewritten toward active Growth Tool
   presentation/state.
6. A later physical-claim validation can therefore produce `WRONG_ITEM_STATE` or an equivalent
   authority/physical mismatch.

The accepted contract gives a broken owner GUI and Repair entry. It does not authorize Branch
mutation while broken.

Required correction:

```text
VALID_BROKEN_OWNER:
  allowsGui = true
  allowsRepair = true
  allowsBranchMutation = false
```

Required regression evidence:

- Broken Tool branch mutation is rejected.
- No DB/session Branch mutation occurs.
- The physical item remains `GRAY_DYE` / `BROKEN_GROWTH_TOOL`.
- Tool ID, Item Instance, Epoch, Progress, and Delivery state remain unchanged.
- Active owner Branch mutation continues to work.

This is a Product Code change after Candidate-4 artifact fixation.

## 3. High-risk authorization-cache gap requiring closure

Candidate-4 schedules full Main-Hand authorization after held-slot, hand-swap, inventory, drop, and
pickup mutations. The current source review did not establish that the old cached result is always
invalidated synchronously before the next managed action.

Potential unsafe window:

```text
old Main Hand:
  cached VALID_ACTIVE_OWNER

inventory/hand mutation:
  stale, non-owner, or old-epoch managed item enters Main Hand

before next-tick full authorization:
  old cached authorization remains observable
```

The inverse case is also unsafe when `NO_MANAGED_ITEM` remains cached while a managed item has
entered Main Hand, because ordinary Block Break permission must not be applied to a managed item.

Candidate-5 must prove one of the following:

1. the existing event path synchronously replaces the cache with a fail-closed managed state before
   any later use can run; or
2. it must implement immediate invalidation/fail-closed replacement before scheduling the next-tick
   full authorization.

Required tests must cover at least:

- valid current Tool -> stale Tool hand transition;
- non-managed item -> stale/non-owner managed Tool transition;
- swap-hand transition;
- number-key or inventory-click transition;
- no Block Break, Progress, GUI, Repair, or Branch action before reauthorization;
- valid current Tool becomes usable after the full authorization completes.

This finding is a source-level security-boundary risk. It must be resolved before real-client use;
it is not recorded as an already reproduced client exploit.

## 4. Frontier late-MVI runtime-test gap

The current `MviReadinessTest` primarily proves `EntryCycleRegistry` state consumption. It does not,
by itself, prove the complete `FrontierGameplayRuntime` behavior required by the work order.

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

The Candidate-3 failure could not be localized because poll-by-poll visibility evidence was absent.
The Candidate-4 timeout line still does not preserve the complete required terminal state.

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

The log must remain sanitized and must not include raw Player UUID. Tests should verify the terminal
snapshot construction or emitted diagnostic message without depending on full Paper log text where
that would make the test brittle.

Changing runtime diagnostics changes Product bytes and therefore belongs to Candidate-5.

## 6. Reproducibility and runtime gates

Candidate-4 retained one preliminary build and one formally qualified clean build. The preliminary
build was byte-identical but lacked the original durable start/end record and was explicitly not
accepted as Formal Build 1.

Candidate-5 must perform two fully qualified clean builds from the exact Candidate-5 Product HEAD.
Both Main and Frontier JARs must match by size and SHA-256 before fixation.

Fresh runtime creation, Plugin Enable, Migration, and real backend preflight were not executed for
Candidate-4. Candidate-5 must complete the authorized disposable runtime preparation outside the
Project Runtime before any Minecraft Client connection.

## 7. Local-only evidence boundary

The Candidate-4 manifest, checksum file, reproducibility records, runtime-preflight record, and
worksheets are recorded as ignored local files. They have not been independently reviewed through a
complete Candidate-4 submission package.

Candidate-5 must produce the complete sanitized submission ZIP and external sidecar required by the
formal instruction. Every required placeholder path must exist even when a gate stops or waits.

## 8. Independent verdict

```text
PHASE 10C-A PRE-CLIENT INDEPENDENT REVIEW:
  FAIL / HOLD

CANDIDATE-4:
  REJECTED BEFORE CLIENT TEST

PRIMARY CONFIRMED PRODUCT DEFECT:
  BROKEN TOOL BRANCH MUTATION ALLOWED

AUTHORIZATION SECURITY BOUNDARY:
  REQUIRES FAIL-CLOSED CACHE-TRANSITION PROOF OR REMEDIATION

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
2. close or conclusively disprove the stale Held Authorization window;
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
