# Phase 10C-A Candidate-5 Remediation Handoff

Recorded: 2026-08-03 JST

## 1. Purpose

This handoff authorizes a narrow Candidate-5 Product remediation after the Candidate-4 pre-client
independent review.

It does not authorize a Minecraft Client connection, Project Runtime change, deployment, merge,
tag, Release, or `requirements_cleared`.

## 2. Starting authority

```text
Repository:
  eariver/Project-Wayfarer-Plugins

Branch:
  feature/V0.0.2-main-frontier

Expected branch HEAD at this handoff:
  b61be3dc5c69e22d507c027f4a3939d2da9330f3

Candidate-4 Product HEAD:
  9fe86d2e787ab1f86dcf38a5abdba6168515a802

Candidate-4:
  REJECTED / IMMUTABLE HISTORICAL EVIDENCE

Candidate-5:
  REQUIRED

PR #14:
  Open / Draft / Unmerged

Client Test:
  DO NOT START
```

Before mutation, verify local, origin, and PR HEAD; clean worktree/index; branch identity; no tag or
Release; and the two review files:

```text
docs/release-readiness/V0.0.2/
  phase-10c-a-candidate-4-preclient-independent-review.md
  phase-10c-a-candidate-4-preclient-independent-review-addendum.md
```

Do not reset, rebase, amend, force push, or overwrite Candidate-4 artifacts.

## 3. Product scope

Candidate-5 is limited to:

1. Broken Tool Branch-mutation denial;
2. fail-closed Held Authorization transitions;
3. actual-held-item guards at managed action entry points;
4. Frontier late-MVI restart coordination proof;
5. complete bounded Frontier TIMEOUT diagnostics;
6. tests and contract/handoff synchronization required by those changes.

No balance, Fixture, migration, permission-model, Resource Pack, unrelated refactor, or Project
Runtime change is authorized.

## 4. Tests-first requirement

Add or update focused tests first. Preserve one RED execution showing the intended Candidate-4
failure before Product implementation.

Minimum Main test coverage:

- `HeldGrowthToolAuthorizationTest` expects `VALID_BROKEN_OWNER.allowsBranchMutation() == false`;
- Broken Tool Branch command/runtime mutation is rejected without session or physical mutation;
- active current Tool Branch mutation remains accepted;
- valid -> stale held-slot transition becomes fail-closed synchronously;
- ordinary -> managed transition becomes fail-closed synchronously;
- swap-hand, number-key/inventory-click, drag, Drop, Pickup, and Respawn transition cases;
- stale cache cannot permit Block Break, Progress, GUI, Repair, Branch, debug, or Damage mutation;
- ordinary item with stale valid cache cannot open the Growth Tool GUI;
- invalid managed item interaction is cancelled;
- ordinary item interaction remains untouched after full `NO_MANAGED_ITEM` authorization.

Minimum Frontier test coverage:

- TIMEOUT plus a concrete MVI MONITOR observation schedules exactly one restart;
- duplicate concrete observations coalesce;
- only one late restart is consumed per external entry cycle;
- quit, actual world leave, newer external entry, and stop cancel pending restart;
- restart reuses the cycle and cannot recursively create unbounded retries;
- terminal TIMEOUT snapshot contains source, generation, poll count, visible/required managed counts,
  fingerprint, and `TIMEOUT` decision.

A Registry-only MVI test is not sufficient.

## 5. Main Product changes

### 5.1 Broken capability

In `HeldGrowthToolAuthorization`:

```text
allowsBranchMutation():
  true only for VALID_ACTIVE_OWNER
```

`VALID_BROKEN_OWNER` retains GUI and Repair entry only.

### 5.2 Synchronous transition state

In `MainGameplayRuntime`, add one helper with equivalent semantics:

```text
failCloseHeldAuthorization(Player or UUID):
  synchronously store AUTHORITY_UNAVAILABLE
  no PDC parse
  no DB/Redis access
  no item mutation
```

Call it before scheduling full `authorizeMainHand(...)` after every operation capable of changing
Main Hand:

- `PlayerItemHeldEvent`;
- `PlayerSwapHandItemsEvent`;
- accepted `InventoryClickEvent`;
- accepted `InventoryDragEvent`;
- `PlayerDropItemEvent`;
- `EntityPickupItemEvent`;
- `PlayerRespawnEvent`;
- authority replacement/refresh before the changed authority is exposed;
- identity/status rewrites when the final authorization is not completed in the same uninterrupted
  main-thread call.

The scheduled full comparison may then replace the transition state with `NO_MANAGED_ITEM`,
`VALID_ACTIVE_OWNER`, `VALID_BROKEN_OWNER`, or an exact invalid state.

### 5.3 Block Break guard

For an actual managed Main-Hand item, permit Block Break only when cached state is exactly
`VALID_ACTIVE_OWNER`.

Do not allow `NO_MANAGED_ITEM` to authorize an actual managed item.

Ordinary item Block Break remains untouched.

### 5.4 Interaction handler

`onInteract(...)` must first classify the actual Main-Hand item.

```text
not managed:
  return without interference

managed + invalid cached state:
  cancel interaction
  open no GUI

managed + VALID_ACTIVE_OWNER or VALID_BROKEN_OWNER:
  cancel interaction
  open GUI
```

Do not reparse the complete claim in the use handler.

### 5.5 Mutation entry points

Require both actual held-item classification and cached capability:

```text
Branch:
  actual managed Tool
  state exactly VALID_ACTIVE_OWNER

Debug held-item mutations:
  actual managed active Tool
  state exactly VALID_ACTIVE_OWNER

Repair snapshot / execution:
  actual managed Tool
  exact active/broken state appropriate to the operation
```

No command or GUI path may rely solely on a stale capability cache.

## 6. Frontier Product changes

### 6.1 Late-restart coordinator

Prefer extracting the late-MVI cycle/restart decision into a small pure component that can be tested
without a running Paper server. The runtime remains responsible for Bukkit task creation and
cancellation, but the component must represent:

```text
external cycle ID
active request generation
timeout state
late restart consumed
restart scheduled
cycle cancelled/completed
```

The design must preserve:

- at most one late restart per external cycle;
- duplicate-event coalescing;
- no recursive new external cycle from the internal restart;
- cancellation on quit, world leave, newer cycle, and stop;
- no repeating or unbounded retry timer.

### 6.2 TIMEOUT observation

Create an immutable terminal observation/snapshot carrying:

```text
source
generation
pollCount
visibleManagedItems
requiredManagedItems
fingerprint
decision
```

The final sanitized TIMEOUT log must include every field and no raw Player UUID.

Retain:

```text
MAX_FINGERPRINT_OBSERVATIONS = 40
FINGERPRINT_POLL_PERIOD_TICKS = 5
REQUIRED_STABLE_OBSERVATIONS = 2
```

Do not replace readiness with an unconditional delay.

## 7. Validation sequence

Run in order and stop at first failure:

1. focused RED tests;
2. focused Main authorization/interaction/Branch tests;
3. focused Frontier readiness/late-restart/diagnostic tests;
4. full Main module tests;
5. full Frontier module tests;
6. `./gradlew check`;
7. `./gradlew clean assemble`;
8. `git diff --check` and scope review;
9. normal Product commit and fast-forward push;
10. exact new PR-head Normal CI and Pre-client Headless Runtime monitoring.

No test may be skipped, disabled, quarantined, or weakened to obtain green.

## 8. Candidate-5 fixation

After Product CI and Headless succeed, perform two independently recorded, fully qualified clean
builds from exact Candidate-5 Product HEAD.

Required equality:

- Main filename, size, and SHA-256;
- Frontier filename, size, and SHA-256;
- binary equality where available.

Core remains the published V0.0.1 authority and is not rebuilt as Candidate-5 Product.
The approved LeafGrapple Fixture remains unchanged.

Use a new staging root and never overwrite Candidate-4:

```text
.ai-work/luna-gpt-5.6-v003/candidate/
  V0.0.2-Client-Candidate-5/
```

## 9. Runtime boundary

`AGENTS.md` prohibits installing JARs, executing migrations, changing runtime configuration,
restarting servers, or deployment from this repository.

Therefore this Product remediation must stop after Candidate-5 artifact fixation and a complete
runtime handoff. It must not start MariaDB, Redis, Paper, Main, Frontier, or a Minecraft Client from
this repository context.

Prepare new isolated runtime authority for a separately authorized runtime task:

```text
MariaDB schema:
  wayfarer_client_v002_c5

Redis prefix:
  wf-v002-client-c5

Main server ID:
  wayfarer-client-c5-main

Frontier server ID:
  wayfarer-client-c5-frontier

Suggested Main port:
  25572

Suggested Frontier port:
  25573
```

A runtime-authorized task must verify the current LAN address and port availability instead of
assuming them.

## 10. Required tracked synchronization

Truthfully update:

- Candidate-4 remains rejected;
- Candidate-5 Product HEAD and fixed artifact identities;
- exact CI/Headless runs and tested SHA classification;
- Client Test not started;
- full Client Acceptance incomplete;
- promotion HOLD;
- Project acceptance pending;
- stable publication not authorized.

PR #14 remains Open / Draft / Unmerged.

## 11. Stop verdict

Successful Product preparation stops with:

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

Any Product-code change after Candidate-5 fixation requires Candidate-6.
