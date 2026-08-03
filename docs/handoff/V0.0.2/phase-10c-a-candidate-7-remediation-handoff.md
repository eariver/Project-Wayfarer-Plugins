# Phase 10C-A Candidate-7 Remediation Handoff

Revision: C  
Recorded: 2026-08-04 JST

## 1. Purpose and role boundary

Candidate-6 was rejected before Server-side Runtime Preflight by:

`docs/release-readiness/V0.0.2/phase-10c-a-candidate-6-independent-product-package-review.md`

This Revision C replaces Revision B. Revision B combined a FIFO serializer, generation tokens,
version guards, eight new runtime scenarios, duplicate local validation, two new formal builds, and a
new evidence ZIP. That was stricter than necessary for the confirmed defect.

Candidate-7 now performs the minimum Product remediation for the confirmed same-Player
Reissue/Revoke/refresh stale-completion defect. The Owner/ChatGPT boundary fixes the behavior and the
required tests below. Luna implements it, executes the listed commands, and reports evidence; Luna
does not choose a substitute design or expand scope.

Non-client validation is authorized, including disposable MariaDB/Redis integration, migrations in
disposable test environments, GitHub Actions service containers, and Headless Paper. Minecraft
Client connection and client-driven scenarios remain deferred.

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

Before Product work:

1. fetch Origin and report Local HEAD, Origin HEAD, PR #14 HEAD/state, branch, and Worktree/Index;
2. verify branch `feature/V0.0.2-main-frontier`;
3. verify Candidate-6 Product HEAD
   `2a3f1cc384c397e610aba33c6ffc0f6a29af2987` and synchronization merge
   `b0bb5a5f2047a87a5313701d5ae46f825aec16d4` are ancestors of Origin HEAD;
4. verify this Revision C handoff, the Candidate-7 Execution Entry Revision B, the Candidate-6
   independent review, and the authority section in `AGENTS.md` are present;
5. verify Candidate-6 artifacts and package are preserved;
6. when Worktree/Index are clean, Local is not ahead or diverged, and Origin HEAD equals PR HEAD,
   update by safe fast-forward only:

```bash
git merge --ff-only origin/feature/V0.0.2-main-frontier
```

Do not use reset, clean, stash, rebase, amend, cherry-pick, or force push. Stop only for an actual
mismatch, dirty state, conflict, failed command, or authority contradiction. Do not stop merely to ask
for confirmation after a passing gate.

Report:

```text
LOCAL HEAD
ORIGIN HEAD
PR HEAD
FAST-FORWARD: PERFORMED / NOT NEEDED / BLOCKED
WORKTREE/INDEX: CLEAN / DIRTY
PR #14: OPEN / DRAFT / UNMERGED or mismatch
CANDIDATE-6 ARTIFACTS / PACKAGE: PRESERVED / NOT VERIFIED
MINECRAFT CLIENT TEST: NOT STARTED / mismatch
NEXT ACTION
```

## 3. Authorized change scope

Product/test changes are limited to:

```text
MODIFY
plugins/wayfarer-main/src/main/java/io/github/eariver/wayfarer/main/gameplay/
MainGameplayRuntime.java

MODIFY
plugins/wayfarer-main/src/test/java/io/github/eariver/wayfarer/main/gameplay/
MainAuthorityFailClosedOrderingTest.java
```

A mechanical fixture-only change to `MainHeldAuthorizationTransitionTest.java` is permitted when
required to compile or expose the deterministic controls used by the prescribed tests. No behavioral
expansion is permitted there.

Do not add a FIFO serializer. Do not change Frontier Product, Core Product, ReissueCoordinator,
GrowthToolDeliveryCoordinator, migrations, permissions, balance, Fixture, Resource Pack, or ordinary
inventory ownership. The initial/pending delivery protocol and paid-reissue transaction protocol are
outside this narrowly confirmed remediation.

## 4. Exact minimal Product design

### 4.1 Per-Player request generation

In `MainGameplayRuntime`, add:

```java
AtomicLong authorityRequestSequence
ConcurrentHashMap<UUID, Long> currentAuthorityRequests
```

Add a private immutable request record containing:

```text
playerUuid
requestGeneration
```

Add a private `beginAuthorityRequest(UUID)` stage whose Bukkit Main Thread action:

1. returns no request when `accepting == false`;
2. increments the global sequence;
3. stores the generation for the Player;
4. replaces Held Authorization with `AUTHORITY_UNAVAILABLE`;
5. returns the request record.

The associated database operation must not be dispatched before this Main Thread action completes.

A request is current only when:

```text
accepting == true
AND currentAuthorityRequests[playerUuid] == requestGeneration
```

### 4.2 Covered operations

Use the request-generation protocol for exactly:

- admin `reissue(UUID)`;
- admin `revoke(UUID)`;
- `refreshSession(UUID)` / `refreshSessionFromAuthority(UUID)`;
- recovery reads initiated by those operations.

`inspect(UUID)`, ordinary held-slot/inventory transitions, initial delivery, and paid-reissue saga
internals remain unchanged.

Each operation may continue to use the repository's existing optimistic CAS behavior. No FIFO,
blocking wait, global lock, custom executor, synchronous JDBC, or synchronous Redis access is added.
Concurrent database work is permitted; stale runtime completion is not.

### 4.3 Completion and result behavior

Add `SUPERSEDED` to `MainGameplayRuntime.AdminMutation`.

Carry the original request through mutation application and recovery. Before any completion changes
Session, inventory, or Held Authorization on the Bukkit Main Thread, check that the request remains
current.

Required behavior:

- stale Reissue/Revoke completion returns `SUPERSEDED` and performs no Session open/close, inventory
  reconciliation, Held Authorization restoration, delivery start, notification, or follow-up refresh;
- stale refresh or stale recovery completion performs no runtime mutation;
- a current successful/no-change/conflict/not-found path preserves its Candidate-6 result semantics;
- a current recoverable failure reads the current database authority and restores that authority;
- a durable `APPLIED` mutation keeps its existing audit record even when its later runtime completion
  is superseded;
- supersession is not reported as `APPLIED`, `CONFLICT`, or `UNAVAILABLE`.

`applyAuthorityMutation(...)`, `recoverAuthoritativeState(...)`, and
`applyAuthoritativeState(...)` must receive the original request. Recovery does not create a new
request generation.

### 4.4 Monotonic Session guard

For a current request with a present database result, compare it with the currently loaded Session
before replacement:

- when current Session `lockVersion` is greater, keep the current Session and do not reconcile from
  the older result;
- when lock versions are equal and Tool ID, Item Instance ID, Instance Epoch, or Status differ, leave
  Held Authorization fail-closed and do not overwrite the Session;
- when lock versions are equal and those authority fields match, preserve the current Session object
  and reauthorize from it rather than overwriting possible uncheckpointed local progress;
- when the database result has a greater lock version, open it, perform the requested reconciliation,
  and authorize Main Hand.

For a current request with an empty result, preserve Candidate-6 behavior: close the Session and leave
Held Authorization fail-closed. For a stale request, an empty result must do nothing.

### 4.5 Quit and stop

`onQuit(...)` must advance/store a new generation for the Player before removing Held Authorization
and starting the existing checkpoint. Its eventual Session close must run on the Bukkit Main Thread
and only while the Player remains offline.

`stopAndFlush()` must set `accepting = false` before clearing request generations and before the
existing final flush. Any already-running completion then fails the current-request predicate and
performs no runtime mutation.

No new lifecycle framework or serializer is required.

## 5. Required tests

### 5.1 One RED test before Product change

Add this deterministic test first and run the focused command in section 6:

```text
newerRequestSupersedesOlderRefreshCompletion
```

The test must hold refresh A after it has captured an older authority, accept a newer mutation/request
B, then complete A and prove A cannot restore the old Session/Held Authorization. Candidate-6 must
fail on the state assertion. Compilation/setup/timeouts do not qualify as RED evidence.

### 5.2 Focused GREEN behaviors

`MainAuthorityFailClosedOrderingTest` must prove these five behaviors. Parameterization or shared
fixtures are allowed; do not create an exhaustive operation Cartesian matrix.

```text
newerRequestSupersedesOlderRefreshCompletion
```

Older refresh result does not open/reconcile/reauthorize after a newer request is accepted.

```text
supersededAppliedMutationSkipsRuntimeApplyAndDelivery
```

An older mutation may durably return `APPLIED`, but after a newer request exists it returns
`SUPERSEDED`, does not expose its authority, and starts no delivery/follow-up refresh.

```text
latestRecoveryRestoresCurrentDatabaseAuthority
```

The latest request's conflict or recoverable failure reads and installs the actual current database
authority.

```text
staleRecoveryCannotCloseOrOverwriteLatestSession
```

A recovery belonging to an older request cannot close or overwrite the Session installed for the
latest request.

```text
quitAndStopObsoleteOutstandingAuthorityCompletion
```

Use two test methods when clearer: quit and stop each prevent an outstanding authority completion
from reopening/restoring runtime state. A rejoin is only required in the quit test when needed to
prove the old completion cannot disturb the new online state.

Retain all existing Candidate-6 fail-close, success/no-change/conflict/failure, stale physical item,
Broken Tool branch, ordinary item, Main transition/action, and Frontier tests. Do not weaken or delete
them.

Use deterministic controllable futures/latches already present in the test fixture. No probabilistic
race loops and no unbounded sleeps.

## 6. Validation commands and gates

Run from repository root and stop at the first actual failure.

### Focused RED, then GREEN

```bash
./gradlew --no-daemon :plugins:wayfarer-main:test \
  --tests 'io.github.eariver.wayfarer.main.gameplay.MainAuthorityFailClosedOrderingTest'
```

Run once before Product change for RED and again after Product change for GREEN.

### Main regression

```bash
./gradlew --no-daemon :plugins:wayfarer-main:test
```

### Repository validation

```bash
./gradlew --no-daemon clean check
./gradlew --no-daemon clean assemble
bash scripts/release/verify-v002-plugin-packaging.sh
git diff --check
git status --short
```

Do not separately rerun Frontier module tests, MariaDB integration tasks, or the unchanged release
policy/manifest/scoped-package validators: `clean check` already reaches both Main and Frontier
`check`, and both module `check` tasks depend on their MariaDB integration tests. The normal CI and
Pre-client Headless workflows provide the remaining repository/headless coverage.

Record commands, results, Java/Gradle identity, and available test totals. Exact per-command start/end
timestamps are not required.

## 7. Commit, CI, artifact fixation, and evidence

After local PASS:

1. confirm the Product/test diff is within section 3;
2. create and normally push one Candidate-7 Product commit;
3. record exact Candidate-7 Product HEAD;
4. monitor Normal CI and Pre-client Headless Runtime to completion and record run IDs, checked-out
   SHA/merge-ref relation, and conclusions;
5. perform one recorded clean artifact build from exact Candidate-7 Product HEAD;
6. stage Candidate-7 Main and Frontier JARs separately from Candidate-6 and record filenames, sizes,
   and SHA-256 values;
7. update only the tracked result/status/runtime-handoff documents made necessary by Candidate-7.

A second formal clean build is not required because Candidate-6 already proved Main/Frontier build
reproducibility and Candidate-7 does not change build logic, resources, or packaging. Run a second
build only when the first result is inconsistent or build/package files unexpectedly change.

A Candidate-7 evidence ZIP and external sidecar are not required. The Product commit, tracked result
record, workflow evidence, clean-build artifact hashes, and final Git/PR state are the independent
review inputs. Do not package duplicate source, raw logs, JARs, runtime configuration, or secrets into
an evidence archive.

Product code/resource changes after artifact fixation reject Candidate-7 and require Candidate-8.
Metadata-only corrections do not.

## 8. Required final report

Report:

```text
CANDIDATE-7 PRODUCT HEAD
FINAL PR HEAD
CHANGED PRODUCT/TEST FILES
RED TEST / ASSERTION
FOCUSED GREEN RESULT
MAIN TEST RESULT
CLEAN CHECK / ASSEMBLE / PACKAGING VALIDATOR
CI RUN ID / CHECKOUT SHA OR MERGE REF / CONCLUSION
HEADLESS RUN ID / CHECKOUT SHA OR MERGE REF / CONCLUSION
CLEAN BUILD ID / MAIN+FRONTIER FILENAME+SIZE+SHA-256
CANDIDATE-7 ARTIFACT PATHS
TRACKED RESULT / RUNTIME HANDOFF PATHS
WORKTREE/INDEX / LOCAL-ORIGIN-PR RELATION
SERVER-SIDE RUNTIME PREFLIGHT STATE
MINECRAFT CLIENT TEST STATE
FINAL VERDICT
```

## 9. Successful stop state

```text
PHASE 10C-A CANDIDATE-7 PRODUCT REMEDIATION:
  PASS

CANDIDATE-7:
  FIXED / PENDING INDEPENDENT REVIEW

NON-CLIENT VALIDATION:
  COMPLETE

SERVER-SIDE RUNTIME PREFLIGHT:
  NOT STARTED / PENDING INDEPENDENT REVIEW

MINECRAFT CLIENT TEST:
  NOT STARTED / DEFERRED

PR #14:
  OPEN / DRAFT / UNMERGED
```
