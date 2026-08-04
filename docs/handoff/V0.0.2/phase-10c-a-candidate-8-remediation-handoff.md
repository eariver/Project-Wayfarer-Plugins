# Phase 10C-A Candidate-8 Remediation Handoff

Revision: A  
Recorded: 2026-08-04 JST

## 1. Authority, purpose, and stop boundary

Candidate-7 was rejected by:

`docs/release-readiness/V0.0.2/phase-10c-a-candidate-7-independent-product-review.md`

The only confirmed Candidate-8 blocker is that an admin Reissue request can pass the existing
start-delivery generation check, wait in `GrowthToolDeliveryCoordinator` database work, become
superseded by a newer same-Player request, and still reach the physical inventory gateway.

The Owner/ChatGPT boundary fixes the Product design, tests, commands, and evidence below. Luna is the
implementation and execution agent. Luna must not substitute another design, expand scope, add
unrequested gates, or weaken the prescribed test. Mechanical imports, formatting, generic typing,
and deterministic test-fixture plumbing are permitted when they do not change semantics.

```text
CANDIDATE-7:
  REJECTED / PRESERVED

CANDIDATE-8:
  REQUIRED

SERVER-SIDE RUNTIME PREFLIGHT:
  DO NOT START BEFORE INDEPENDENT CANDIDATE-8 REVIEW

MINECRAFT CLIENT TEST:
  NOT STARTED / DEFERRED
```

Non-client validation remains authorized, including disposable MariaDB/Redis integration reached by
repository checks, GitHub Actions service containers, and Pre-client Headless Paper.

This handoff does not authorize PR merge/Ready transition, tag, Release, Project Issue #4 change,
Fixture change, `requirements_cleared`, Project Runtime modification, or Minecraft Client connection.

## 2. Recovery gate

Before Product work:

1. fetch Origin and report Local HEAD, Origin HEAD, PR #14 HEAD/state, branch, and Worktree/Index;
2. verify branch `feature/V0.0.2-main-frontier`;
3. verify Candidate-7 Product HEAD
   `980eda20921a5f3ae1f795a2b9a23b92f53ac8e2` and Candidate-7 result commit
   `a2ffc6a48ee5d29c6ceb961bc7a453534b9e797d` are ancestors of Origin HEAD;
4. verify this handoff, the Candidate-7 independent review, and the specification/implementation
   authority section in `AGENTS.md` are present in the Origin tree;
5. verify Candidate-6 and Candidate-7 local artifacts/evidence remain preserved and separate;
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
CANDIDATE-7 ARTIFACTS / EVIDENCE: PRESERVED / NOT VERIFIED
MINECRAFT CLIENT TEST: NOT STARTED / mismatch
NEXT ACTION
```

When the gate passes, continue without waiting for another confirmation.

## 3. Exact authorized change scope

Candidate-8 Product/test changes are limited to:

```text
MODIFY
plugins/wayfarer-main/src/main/java/io/github/eariver/wayfarer/main/application/
GrowthToolDeliveryCoordinator.java

MODIFY
plugins/wayfarer-main/src/main/java/io/github/eariver/wayfarer/main/gameplay/
MainGameplayRuntime.java

MODIFY
plugins/wayfarer-main/src/test/java/io/github/eariver/wayfarer/main/application/
GrowthToolDeliveryCoordinatorTest.java

MODIFY
plugins/wayfarer-main/src/test/java/io/github/eariver/wayfarer/main/gameplay/
MainAuthorityFailClosedOrderingTest.java
```

A fixture-only addition exposing `PlayerInventory` or deterministic pending-operation controls inside
the two authorized test files is permitted. No other Product or test file may change without stopping
and reporting the exact need.

Do not change Core, Frontier, repository schema/migrations, `GrowthTool`, ReissueCoordinator, paid
reissue transaction semantics, prices, permissions, balance, Fixture, Resource Pack, normal inventory
ownership, build logic, or workflow definitions.

Do not introduce FIFO serialization, locks, blocking waits, a new executor, synchronous JDBC/Redis,
or a new lifecycle framework.

## 4. Exact minimal Product design

### 4.1 Delivery coordinator admission overload

In `GrowthToolDeliveryCoordinator`, preserve the existing public method:

```java
CompletionStage<Outcome> onJoin(UUID playerUuid)
```

Make it delegate to one new overload:

```java
CompletionStage<Outcome> onJoin(
    UUID playerUuid,
    Predicate<GrowthTool> deliveryAdmission
)
```

Required semantics:

1. `playerUuid` and `deliveryAdmission` are non-null;
2. the existing `onJoin(UUID)` path uses an always-true admission and retains all existing initial
   delivery and retry behavior;
3. repository `findOrCreate(...)` remains asynchronous exactly as today;
4. for a `PENDING` tool, evaluate `deliveryAdmission.test(tool)` inside the coordinator's existing
   Bukkit Main Thread task, immediately before `gateway.deliverIfEligible(tool)`;
5. when admission is false, do not invoke the gateway and return `Outcome.SUPERSEDED`;
6. add `SUPERSEDED` to `GrowthToolDeliveryCoordinator.Outcome`;
7. `completeDelivery(...)` must return `SUPERSEDED` immediately without calling `markDelivered(...)`
   and without recording a delivery-pending/delivery-result audit for that obsolete attempt;
8. all other outcomes and diagnostics remain unchanged.

The admission check and gateway call must be in the same Main Thread runnable with no asynchronous
boundary between them. This is the linearization boundary that prevents a newer Bukkit Main Thread
request from interleaving between the final currency check and physical inventory mutation.

### 4.2 Main runtime request-aware delivery

Preserve Candidate-7's existing generation model, `SUPERSEDED` admin result, Session guards, quit/stop
invalidation, and the existing start check in `startAdminDelivery(...)`.

Change only the coordinator invocation for admin Reissue so it uses the new overload and supplies:

```java
ignoredTool -> isCurrent(request)
```

The predicate is evaluated by the coordinator on the Bukkit Main Thread immediately before the
physical delivery gateway. Do not call `currentOnMainThread(...)` from inside the predicate and do not
start another asynchronous stage there.

When admission fails:

- no item is created or inserted;
- no delivery mark is written;
- no delivery notification is sent;
- no follow-up refresh is started;
- the admin Reissue result remains `AdminMutation.SUPERSEDED` through the existing Candidate-7 flow.

Initial join delivery and explicit retry delivery continue to call the existing `onJoin(UUID)` method
and are not generation-gated by Candidate-8.

Do not modify `MainGameplayRuntime.deliver(...)`; the new coordinator admission runs in the same Main
Thread task immediately before that method is invoked.

## 5. Exact tests

### 5.1 Tests-first RED

Before Product code changes, add this deterministic runtime test to
`MainAuthorityFailClosedOrderingTest`:

```text
deliveryStartedThenSupersededBeforeGatewaySkipsPhysicalMutation
```

Required sequence:

1. load an old delivered authority in Session;
2. configure admin Reissue A to durably return a rotated `PENDING` authority;
3. start A and complete only A's authority-mutation database stage, so A has passed
   `startAdminDelivery(...)` and its delivery coordinator database stage is pending;
4. accept newer Revoke/authority request B for the same Player, advancing the request generation;
5. complete A's pending delivery coordinator database stage;
6. assert A returns `AdminMutation.SUPERSEDED`;
7. assert `PlayerInventory.addItem(...)` was never called;
8. assert `GrowthToolRepository.markDelivered(...)` was never called;
9. do not require B's pending database stage to complete for this focused assertion.

The fixture may expose `PlayerInventory`, stub `firstEmpty()` and `addItem(...)`, and retain the
existing deterministic `ControlledTasks` queue.

Run the focused command in section 6 before Product changes. Candidate-7 must fail because
`addItem(...)` is invoked after B is accepted. The failure must be the physical-mutation assertion;
compilation/setup errors, hangs, or timeouts do not qualify as RED evidence.

### 5.2 Direct coordinator contract test

After recording RED, add this test to `GrowthToolDeliveryCoordinatorTest`:

```text
rejectedDeliveryAdmissionSkipsGatewayAndMarkDelivered
```

Use a `PENDING` tool and call the new overload with an admission predicate returning false. The test
must assert:

- result is `Outcome.SUPERSEDED`;
- the gateway is not invoked;
- repository `markDelivered(...)` is not invoked.

The existing direct task fixture is sufficient; no timing loop or sleep is required.

### 5.3 Regression preservation

Retain all Candidate-7 request-generation, stale completion, recovery, quit/stop, fail-close, stale
physical item, Broken Tool, ordinary item, and delivery coordinator tests. Do not weaken, delete,
disable, or quarantine existing tests.

Do not add an exhaustive operation Cartesian matrix. These two new tests are the complete mandatory
Candidate-8 test addition.

## 6. Exact validation commands and gates

Run from repository root and stop at the first actual failure.

### Focused RED before Product change

```bash
./gradlew.bat --no-daemon :plugins:wayfarer-main:test --tests "io.github.eariver.wayfarer.main.gameplay.MainAuthorityFailClosedOrderingTest"
```

Record the failing method and physical-mutation assertion.

### Focused GREEN after Product change

```bash
./gradlew.bat --no-daemon :plugins:wayfarer-main:test --tests "io.github.eariver.wayfarer.main.gameplay.MainAuthorityFailClosedOrderingTest" --tests "io.github.eariver.wayfarer.main.application.GrowthToolDeliveryCoordinatorTest"
```

### Main regression

```bash
./gradlew.bat --no-daemon :plugins:wayfarer-main:test
```

### Repository validation

```bash
./gradlew.bat --no-daemon clean check
./gradlew.bat --no-daemon clean assemble
bash scripts/release/verify-v002-plugin-packaging.sh
git diff --check
git status --short
```

Do not separately rerun unchanged Frontier tests, standalone integration tasks, release-policy
validators, manifest validators, or scoped-package validators. Repository `clean check`, Normal CI,
and Pre-client Headless provide the required broader coverage.

Record commands, results, Java/Gradle identity, and available test totals. Exact command timestamps
are not a gate.

## 7. Product commit, CI, artifact fixation, and evidence

After local PASS:

1. confirm the Product/test diff contains only the four files in section 3;
2. create and normally push one clearly identified Candidate-8 Product commit;
3. record exact Candidate-8 Product HEAD;
4. monitor Normal CI and Pre-client Headless Runtime to completion;
5. record run IDs, conclusions, event/head SHA, checkout or PR merge-ref SHA, and relation to Product
   HEAD;
6. perform one recorded clean artifact build from exact Candidate-8 Product HEAD;
7. stage Candidate-8 Main and Frontier JARs separately from Candidate-6 and Candidate-7, recording
   filenames, sizes, and SHA-256 values;
8. add tracked Candidate-8 Product result and unexecuted Runtime Handoff records, and update
   `docs/work-orders/V0.0.2/execution-status.md` to the factual state;
9. normally push the metadata-only evidence commit.

A second formal clean build is not required because Candidate-8 does not modify build logic,
resources, or packaging. Run a second build only when the first build is inconsistent or an
unexpected build/package file changes.

A Candidate-8 evidence ZIP or sidecar is not required. The Product commit, tracked result/runtime
handoff, workflow evidence, artifact sizes/hashes, and final Git/PR state are sufficient independent
review inputs.

Any Product code/resource change after Candidate-8 artifact fixation rejects Candidate-8 and requires
Candidate-9. Metadata-only corrections do not.

## 8. Required final report

Report:

```text
CANDIDATE-8 PRODUCT HEAD
FINAL PR HEAD
CHANGED PRODUCT/TEST FILES
RED TEST / ASSERTION
FOCUSED GREEN RESULT
MAIN TEST RESULT
CLEAN CHECK / ASSEMBLE / PACKAGING VALIDATOR
CI RUN ID / HEAD OR MERGE REF / CONCLUSION
HEADLESS RUN ID / HEAD OR MERGE REF / CONCLUSION
CLEAN BUILD ID / MAIN+FRONTIER FILENAME+SIZE+SHA-256
CANDIDATE-8 ARTIFACT PATHS
TRACKED RESULT / RUNTIME HANDOFF PATHS
WORKTREE/INDEX / LOCAL-ORIGIN-PR RELATION
SERVER-SIDE RUNTIME PREFLIGHT STATE
MINECRAFT CLIENT TEST STATE
FINAL VERDICT
```

## 9. Successful stop state

```text
PHASE 10C-A CANDIDATE-8 PRODUCT REMEDIATION:
  PASS

CANDIDATE-7:
  REJECTED / PRESERVED

CANDIDATE-8:
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
