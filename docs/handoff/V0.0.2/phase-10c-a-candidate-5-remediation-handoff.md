# Phase 10C-A Candidate-5 Remediation Handoff

Revision: A  
Recorded: 2026-08-03 JST

## 1. Purpose and stopping boundary

This handoff authorizes a narrow Candidate-5 Product remediation after the Candidate-4 pre-client
independent review.

It does not authorize:

- a Minecraft Client connection;
- installation into Project Wayfarer Runtime;
- MariaDB or Redis runtime mutation;
- Paper start/restart or deployment;
- merge, Ready for Review, tag, or Release;
- Project Issue #4 modification;
- `requirements_cleared`.

Successful execution stops after Candidate-5 Product validation, two-formal-build fixation, tracked
status synchronization, and creation of a complete runtime handoff and review package.

## 2. Starting authority and recovery gate

```text
Repository:
  eariver/Project-Wayfarer-Plugins

Branch:
  feature/V0.0.2-main-frontier

Minimum required review commit:
  70b66e17b7308ae4c9529a1685f820e8a7773bfa

Required review addendum commit:
  b61be3dc5c69e22d507c027f4a3939d2da9330f3

Initial Candidate-5 handoff publication commit:
  24684652b00ae6e5f9cd00215f8f2fe237cc9ef1

Complete Candidate-5 handoff revision commit:
  resolved from this file at execution start

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

The branch may have later documentation-only commits. Before mutation, Luna must report and verify:

```bash
git branch --show-current
git status --porcelain=v1
git diff --exit-code
git diff --cached --exit-code
git rev-parse HEAD
git fetch origin
git rev-parse origin/feature/V0.0.2-main-frontier
git merge-base --is-ancestor 70b66e17b7308ae4c9529a1685f820e8a7773bfa HEAD
git merge-base --is-ancestor b61be3dc5c69e22d507c027f4a3939d2da9330f3 HEAD
git merge-base --is-ancestor 24684652b00ae6e5f9cd00215f8f2fe237cc9ef1 HEAD
git log --oneline --decorate 9fe86d2e787ab1f86dcf38a5abdba6168515a802..HEAD
git tag --list 'V0.0.2*'
git ls-remote --tags origin 'V0.0.2*'
gh pr view 14 --json state,isDraft,mergedAt,mergeable,mergeStateStatus,headRefOid,headRefName,baseRefName
gh release view V0.0.2
```

Required starting conditions:

- local, origin, and PR HEAD are identical;
- worktree and index are clean;
- current HEAD descends from all required review/handoff commits;
- no unexplained Product change exists after Candidate-4 Product HEAD;
- PR #14 remains Open, Draft, Unmerged, and non-conflicting;
- no V0.0.2 tag or Release exists.

STOP without reset, clean, stash, rebase, amend, force push, or artifact deletion when any condition
is not satisfied.

Required review files:

```text
docs/release-readiness/V0.0.2/
  phase-10c-a-candidate-4-preclient-independent-review.md
  phase-10c-a-candidate-4-preclient-independent-review-addendum.md
```

Candidate-4 fixed JARs, manifests, worksheets, and local evidence must not be overwritten.

## 3. Authorized Product scope

Candidate-5 is limited to:

1. Broken Tool Branch-mutation denial;
2. fail-closed Held Authorization transitions;
3. actual-held-item guards at managed action entry points;
4. Frontier late-MVI restart coordination proof;
5. complete bounded Frontier TIMEOUT diagnostics;
6. tests and contract/traceability/handoff synchronization required by those changes.

No balance, Fixture, migration, permission-model, Resource Pack, unrelated refactor, or Project
Runtime change is authorized.

## 4. Tests-first requirement

Add or update focused tests first. Preserve one RED execution showing the intended Candidate-4
failure before Product implementation.

Required RED evidence must record:

- exact Candidate-4 Product baseline or equivalent source state;
- exact commands;
- expected failing assertions;
- actual failing assertions;
- confirmation that failures are caused by the intended missing behavior, not compilation or test
  setup errors.

Minimum Main coverage:

- `VALID_BROKEN_OWNER.allowsBranchMutation() == false`;
- Broken Tool Branch mutation is rejected without session or physical mutation;
- active current Tool Branch mutation remains accepted;
- valid current Tool -> stale/old-epoch managed item transition becomes fail-closed synchronously;
- ordinary item -> managed item transition becomes fail-closed synchronously;
- held-slot, swap-hand, number-key/inventory-click, drag, Drop, Pickup, and Respawn transitions;
- stale cache cannot permit Block Break, Progress, GUI, Repair, Branch, debug, or managed Damage
  mutation;
- ordinary item with stale valid cache does not open Growth Tool GUI;
- invalid managed item interaction is cancelled and opens no GUI;
- ordinary item interaction remains untouched after full `NO_MANAGED_ITEM` authorization;
- failed/conflicted authority mutation restores a correct authorization for the still-current
  authority rather than leaving a permanent transition denial.

Minimum Frontier coverage:

- TIMEOUT plus a concrete MVI MONITOR observation schedules exactly one next-tick restart;
- duplicate concrete observations coalesce;
- only one late restart is consumed per external entry cycle;
- quit, actual world leave, newer external entry, and stop cancel pending restart;
- the internal restart reuses the cycle and cannot recursively create unbounded retries;
- MVI absent/disabled retains the native Bukkit fingerprint path;
- terminal TIMEOUT snapshot contains source, generation, poll count, visible/required managed counts,
  fingerprint, and `TIMEOUT` decision.

A Registry-only MVI test is insufficient.

## 5. Main Product changes

### 5.1 Broken capability

In `HeldGrowthToolAuthorization`:

```text
allowsBranchMutation():
  true only for VALID_ACTIVE_OWNER
```

`VALID_BROKEN_OWNER` retains GUI and Repair entry only.

### 5.2 Synchronous fail-closed transition

In `MainGameplayRuntime`, add one helper with equivalent semantics:

```text
failCloseHeldAuthorization(Player or UUID):
  synchronously store AUTHORITY_UNAVAILABLE or an equivalent managed fail-closed transition state
  deny all managed capabilities
  perform no PDC parse
  perform no DB/Redis access
  perform no item mutation
```

Call it before scheduling full `authorizeMainHand(...)` after every accepted operation capable of
changing Main Hand:

- `PlayerItemHeldEvent`;
- `PlayerSwapHandItemsEvent`;
- accepted `InventoryClickEvent`;
- accepted `InventoryDragEvent`;
- `PlayerDropItemEvent`;
- `EntityPickupItemEvent`;
- `PlayerRespawnEvent`.

For an event that is cancelled by Wayfarer, do not leave the Player permanently fail-closed; either
do not transition the cache or immediately restore authorization for the unchanged Main Hand.

The scheduled full comparison may replace the transition state with `NO_MANAGED_ITEM`,
`VALID_ACTIVE_OWNER`, `VALID_BROKEN_OWNER`, or an exact invalid state.

### 5.3 Authority and identity mutation ordering

For Reissue, Revoke, authority refresh, ACTIVE -> BROKEN, BROKEN -> ACTIVE, and any identity rewrite:

1. fail-close the old cached result before the changed authority/item becomes observable;
2. perform persistence or physical mutation through the existing authoritative path;
3. on success, install the new Session authority/item and run full authorization before later
   managed use;
4. on conflict, failure, cancellation, or no-change, reload or retain the still-authoritative state
   and reauthorize it;
5. never leave the Player permanently `AUTHORITY_UNAVAILABLE` solely because an asynchronous
   mutation failed;
6. never expose a cache produced for an older Item Instance or Epoch after a durable rotation.

Do not introduce synchronous DB access in Bukkit event handlers.

### 5.4 Block Break and Progress

For an actual managed Main-Hand item, the HIGHEST guard permits Block Break only when cached state is
exactly `VALID_ACTIVE_OWNER`.

`NO_MANAGED_ITEM` must never authorize an actual managed item.

Ordinary item Block Break remains untouched.

The MONITOR Progress handler remains non-cancelling, ignores already-cancelled events, and records
Progress only for `VALID_ACTIVE_OWNER`.

### 5.5 Interaction handler

`onInteract(...)` must first classify the actual Main-Hand item.

```text
not managed:
  return without interference

managed + invalid/transition cached state:
  cancel interaction
  open no GUI

managed + VALID_ACTIVE_OWNER or VALID_BROKEN_OWNER:
  cancel interaction
  open GUI
```

Do not reparse the complete claim in the use handler.

### 5.6 Mutation entry points

Require both actual held-item classification and exact cached capability:

```text
Branch:
  actual managed Tool
  state exactly VALID_ACTIVE_OWNER

Debug held-item mutation:
  actual managed active Tool
  state exactly VALID_ACTIVE_OWNER

Repair snapshot / execution:
  actual managed Tool
  exact active/broken state appropriate to the operation
```

No command or GUI path may rely solely on a stale capability cache. Lightweight managed-item
classification is permitted; per-action full PDC/DB authorization is not.

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
- cancellation on quit, actual world leave, newer cycle, and stop;
- no repeating or unbounded retry timer;
- native Bukkit fingerprint behavior when MVI is absent or disabled.

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

## 7. Documentation and traceability updates

Update only the minimum tracked contracts needed to keep the new Product truthful:

- Decision Register;
- Main/Frontier requirements and source provenance where affected;
- traceability;
- Client Acceptance plan;
- blocking/execution status;
- release-readiness and handoff records;
- PR #14 body.

Record:

- Candidate-4 rejected and preserved;
- Candidate-5 required, then exact Product/fixation status after execution;
- the two confirmed Main defects and Frontier proof/diagnostic gaps;
- Client Test not started;
- Full Client Acceptance incomplete;
- promotion HOLD;
- Project acceptance pending;
- stable publication not authorized.

Do not modify Project Issue #4 or the approved Fixture.

## 8. Local validation sequence

Run in order and stop at the first failure:

1. focused RED tests;
2. focused Main authorization/interaction/Branch tests;
3. focused Frontier readiness/late-restart/diagnostic tests;
4. full Main module tests;
5. full Frontier module tests;
6. `./gradlew check`;
7. `./gradlew clean assemble`;
8. release/package validators required by the current repository baseline;
9. `git diff --check`;
10. changed-file and scope review.

Record exact command, Java and Gradle identity, start/end time, result, test count, and skipped count.

No test may be skipped, disabled, quarantined, or weakened to obtain green.

## 9. Product commit, push, and workflow evidence

After local PASS:

1. create one Candidate-5 Product commit containing Product code, tests, and defining contracts;
2. push by normal fast-forward only;
3. record exact Candidate-5 Product HEAD;
4. monitor the new Normal CI and Pre-client Headless Runtime to completion.

For each workflow, record separately:

```text
event head SHA
PR head SHA
workflow checkout SHA
PR merge-ref SHA, when used
Candidate-5 Product HEAD
relationship between the tested tree and Product HEAD
```

When the workflow checks out `refs/pull/14/merge`, report:

```text
PR_MERGE_REF_VALIDATION:
  PASS or FAIL

EXACT_PRODUCT_HEAD_CHECKOUT:
  NOT PERFORMED
```

Do not describe PR merge-ref validation as a direct Product-HEAD checkout. Do not modify a workflow
merely to manufacture a different evidence classification.

STOP when either workflow fails, targets an unexpected event head, or tests an unexplained tree.

## 10. Candidate-5 reproducible fixation

After Product CI and Headless succeed, perform two independently recorded, fully qualified clean
builds from exact Candidate-5 Product HEAD.

Each formal build must record:

- clean detached worktree or equivalent proven clean checkout;
- exact Product HEAD;
- exact command;
- Java and Gradle versions;
- start/end time;
- Main and Frontier filename, size, SHA-256;
- binary comparison where available.

Required equality:

- Main same filename, size, SHA-256, and bytes;
- Frontier same filename, size, SHA-256, and bytes.

Core remains the published V0.0.1 authority:

```text
Wayfarer_Core-V0.0.1.jar
size 11751447
SHA-256 b045581d3984dddba10ed7b2ada435926b8538ba9b29a1151550ce59588395a2
```

The approved Fixture remains unchanged:

```text
docs/testing/fixtures/V0.0.2/leafgrapple-wayfarer-test-only.yml
SHA-256 ed210f8e56db26315f91fecb9e1d35d686c8fe647480498b7588467a6fa2448a
```

Use a new local staging root and never overwrite Candidate-4:

```text
.ai-work/luna-gpt-5.6-v003/candidate/
  V0.0.2-Client-Candidate-5/
```

Any Product-code change after fixation rejects Candidate-5 and requires Candidate-6.

## 11. Candidate-5 evidence and submission package

Prepare local-only evidence without committing JARs, worlds, DB/Redis data, full logs, secrets, or
credentials.

Required Candidate files:

```text
CANDIDATE_5_MANIFEST.md
CANDIDATE_5_SHA256SUMS.txt
CLIENT_RETEST_HANDOFF.md
OWNER_BIND_CHANGE_SUMMARY.md
FRONTIER_READINESS_CHANGE_SUMMARY.md
```

Prepare a sanitized submission ZIP and external sidecar under the V003 submission root. It must
contain at least:

- complete Candidate-5 result report;
- Candidate-4 review acknowledgement;
- RED and green test evidence;
- Product changed-file list/stat/patch;
- CI and Headless evidence with SHA classification;
- two-formal-build reproducibility evidence;
- Candidate-5 manifest and checksums;
- runtime handoff;
- PR/Git final state;
- placeholders with explicit `NOT_CREATED_*` or `NOT_STARTED_*` status for every required but
  intentionally unavailable runtime/client artifact.

Internal `SHA256SUMS.txt` must:

- hash every archive file except itself;
- use lowercase 64-character SHA-256;
- use two spaces before each relative POSIX path;
- have no unlisted or missing archive file.

Before delivery, validate:

1. ZIP integrity;
2. every internal checksum;
3. all archive files are listed except the checksum file itself;
4. every README/report reference exists in the archive;
5. external sidecar target filename and final ZIP hash match.

## 12. Runtime boundary and separate handoff

`AGENTS.md` prohibits installing JARs, executing migrations, changing runtime configuration,
restarting servers, or deployment from this Plugin repository context.

Therefore this Product task must not start MariaDB, Redis, Paper, Main, Frontier, or a Minecraft
Client.

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

The runtime-authorized task must verify the current LAN address and port availability rather than
assuming them. It must use only fixed Candidate-5 bytes, create fresh DB/Redis/world/player/MVI
state, run server-side preflight, and stop before the first Client action for a new independent
review.

## 13. Final Git and PR gate

Before stopping, verify:

```bash
git status --porcelain=v1
git diff --exit-code
git diff --cached --exit-code
git rev-parse HEAD
git ls-remote origin refs/heads/feature/V0.0.2-main-frontier
gh pr view 14 --json state,isDraft,mergedAt,mergeable,mergeStateStatus,headRefOid
git tag --list 'V0.0.2*'
git ls-remote --tags origin 'V0.0.2*'
gh release view V0.0.2
```

Required:

- tracked worktree/index clean;
- local/origin/PR HEAD identical;
- PR Open, Draft, Unmerged;
- no V0.0.2 tag or Release;
- PR body truthfully reflects Candidate-5 state;
- no Candidate JAR or prohibited runtime evidence tracked;
- Candidate-4 artifacts unchanged.

## 14. Stop verdict

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

Any Product failure after fixation records:

```text
CANDIDATE-5:
  REJECTED

CANDIDATE-6:
  REQUIRED
```
