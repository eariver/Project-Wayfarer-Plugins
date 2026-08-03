# Phase 10C-A Candidate-7 Remediation Handoff

Revision: B  
Recorded: 2026-08-04 JST

## 1. Authority and execution roles

Candidate-6 was rejected before Server-side Runtime Preflight by:

`docs/release-readiness/V0.0.2/phase-10c-a-candidate-6-independent-product-package-review.md`

This Revision B replaces Revision A as the execution authority for Candidate-7.

The Product design and test contract below are fixed by the Owner/ChatGPT review boundary. Luna is
not asked to choose an algorithm, reconcile alternatives, expand scope, or devise acceptance tests.
Luna must implement the specified design, run the specified tests and validation commands, and report
the resulting evidence.

Only mechanical implementation adjustments are permitted when they preserve every specified
semantic, for example imports, formatting, generic typing, or test-fixture plumbing. When the design
cannot be implemented exactly without another Product decision, Luna must stop before substituting a
different design and report the exact conflict.

This handoff does not authorize PR merge/Ready transition, tag, Release, Project Issue #4 change,
Fixture change, `requirements_cleared`, Minecraft Client connection, or client-driven scenarios.

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
4. verify the Candidate-6 independent review, this Revision B handoff, and the repository-level
   specification/implementation authority section in `AGENTS.md` are present in the Origin tree;
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

## 3. Exact authorized file scope

Candidate-7 Product and focused-test changes are limited to these files:

```text
ADD
plugins/wayfarer-main/src/main/java/io/github/eariver/wayfarer/main/gameplay/
MainAuthorityOperationSerializer.java

MODIFY
plugins/wayfarer-main/src/main/java/io/github/eariver/wayfarer/main/gameplay/
MainGameplayRuntime.java

ADD
plugins/wayfarer-main/src/test/java/io/github/eariver/wayfarer/main/gameplay/
MainAuthorityOperationSerializerTest.java

MODIFY
plugins/wayfarer-main/src/test/java/io/github/eariver/wayfarer/main/gameplay/
MainAuthorityFailClosedOrderingTest.java
```

A mechanical fixture-only change to `MainHeldAuthorizationTransitionTest.java` is permitted only when
required to compile against the exact runtime signature changes below. No behavioral expansion is
permitted there.

Do not modify Frontier Product code, Core Product code, migration files, permissions, balance,
Fixture, Resource Pack, ordinary inventory ownership, or unrelated architecture. Do not move the
existing Frontier serializer into Common and do not create a Main-to-Frontier dependency.

## 4. Exact Product design

### 4.1 Per-player FIFO serializer

Create package-private final class `MainAuthorityOperationSerializer` in the Main gameplay package.
Its behavior must be the Main-local equivalent of the existing Frontier
`PlayerOperationSerializer`, without importing or depending on the Frontier module.

Required public/package API:

```java
<T> CompletionStage<T> enqueue(
    UUID playerUuid,
    Supplier<CompletionStage<T>> operation
)

void shutdown()

boolean accepting()
```

Required semantics:

1. operations for the same Player start in registration order;
2. the next same-Player supplier does not start until the previous returned stage completes, whether
   normally or exceptionally;
3. different Players may run concurrently;
4. one failed operation does not poison the Player tail;
5. `shutdown()` rejects newly registered operations and prevents queued-but-not-started suppliers
   from starting;
6. a currently running supplier is not forcibly interrupted;
7. completed tails are removed with conditional identity removal so one completion cannot remove a
   newer tail;
8. nested same-Player `enqueue` from inside an operation supplier is forbidden by design and must not
   be introduced by `MainGameplayRuntime`.

Use `ConcurrentHashMap<UUID, CompletableFuture<?>>`, `AtomicBoolean`, and the same non-blocking
CompletionStage chaining model already proven by the Frontier serializer. Do not introduce locks,
blocking waits, a global single-thread executor, or synchronous database access.

### 4.2 Authority request identity

In `MainGameplayRuntime`, add exactly one global monotonic sequence and one current-generation map:

```java
AtomicLong authorityGenerationSequence
ConcurrentHashMap<UUID, Long> authorityGenerations
```

Add one `MainAuthorityOperationSerializer` field.

Add a private immutable request record with these values:

```text
playerUuid
requestGeneration
baselineToolId, nullable
baselineLockVersion, Long.MIN_VALUE when no Session authority exists
```

The request is accepted on the Bukkit Main Thread. Acceptance is the linearization point for
same-Player request order.

Add a private `beginAuthorityRequest(UUID)` stage. Its Main Thread action must, in this order:

1. reject/return no request when `accepting == false`;
2. read the current Session authority for the baseline Tool ID and lock version;
3. increment the global sequence;
4. store the new generation for that Player;
5. replace Held Authorization with `AUTHORITY_UNAVAILABLE`;
6. return the immutable request record.

The database stage must not be dispatched before this Main Thread action completes.

Add a private current-request predicate. It is true only when:

```text
accepting == true
AND authorityGenerations[playerUuid] == requestGeneration
```

Add a private Main Thread invalidation method for quit/lifecycle boundaries. It increments the global
sequence and stores the new generation for that Player without starting a database operation.

### 4.3 Operations that must use the same request protocol

The following flows must call `beginAuthorityRequest(...)` and then register their complete async
body in `MainAuthorityOperationSerializer.enqueue(...)`:

1. `open(Player)` authority load/reconciliation;
2. admin `revoke(UUID)` / `reissue(UUID)` through the internal authority replacement operation;
3. `refreshSession(UUID)` / `refreshSessionFromAuthority(UUID)`;
4. the refresh portion reached from retry delivery or paid-reissue completion.

The serialized body includes its database read/mutation, conflict/failure recovery read, and final
Main Thread state-application stage. Do not enqueue the recovery stage as a second nested operation;
it remains part of the original request's serialized body.

`inspect(UUID)` is read-only and does not change Session/Held Authorization, so it does not enter this
serializer.

Ordinary held-slot/inventory transition authorization is unchanged and does not enter this
serializer.

### 4.4 Admin mutation result

Add `SUPERSEDED` to `MainGameplayRuntime.AdminMutation`.

Use a private internal result record that carries both the `AdminMutation` result and the immutable
authority request until the complete admin flow is finished.

Required result behavior:

- a durable mutation that remains the current request returns its existing result, including
  `APPLIED`, `NO_CHANGE`, `CONFLICT`, `NOT_FOUND`, or `UNAVAILABLE`;
- a request whose generation is no longer current must not apply Session, inventory, or Held
  Authorization state and returns `SUPERSEDED` to the caller;
- `reissue(UUID)` must start no delivery/notification/follow-up refresh when its mutation result is
  `SUPERSEDED`;
- immediately before starting reissue delivery after an `APPLIED` result, re-check on the Main Thread
  that the original request is still current; return `SUPERSEDED` instead when it is not;
- when an `APPLIED` database mutation became superseded before runtime application, retain the
  existing durable admin audit event. Supersession prevents runtime state exposure; it does not erase
  the fact that the durable mutation occurred.

Do not map supersession to `APPLIED`, `CONFLICT`, or `UNAVAILABLE`.

### 4.5 Recovery and state application

Change `applyAuthorityMutation(...)`, `recoverAuthoritativeState(...)`, and
`applyAuthoritativeState(...)` so the original immutable request is passed through every stage.
Recovery must not mint a new generation.

`applyAuthoritativeState(...)` must execute on the Bukkit Main Thread and apply these checks in this
order:

1. when the request is no longer current, return a stale/not-applied result without mutating Session,
   inventory, or Held Authorization;
2. when `accepting == false`, return stale/not-applied;
3. obtain the currently loaded Session authority, when any;
4. for an empty authoritative database result:
   - when the current Session was created or advanced after the request baseline
     (`baselineToolId` differs, the baseline had no authority, or current lock version is greater than
     `baselineLockVersion`), treat the empty result as stale and leave the newer Session untouched;
   - otherwise close the Session and leave Held Authorization fail-closed, preserving Candidate-6
     behavior for an actually unresolvable current authority;
5. for a present authoritative result:
   - owner UUID and Tool ID must match the Player/request baseline when a baseline exists;
   - reject as stale when the currently loaded same-Tool Session has a greater `lockVersion`;
   - when lock versions are equal, require equal Item Instance ID, Instance Epoch, and Status;
     otherwise leave Held Authorization fail-closed and do not overwrite the Session;
   - accept a greater lock version or an exactly idempotent equal version;
6. only after those checks, call `sessions.open(...)`, optional inventory reconciliation, and
   `authorizeMainHand(...)`.

No Bukkit object may cross into a database supplier. No synchronous JDBC/Redis access may be added.

### 4.6 Direct paid-reissue delivery guard

`deliverReissued(GrowthTool rotatedTool)` remains synchronous on the Bukkit Main Thread and does not
enter the FIFO serializer because the `ReissueCoordinator` contract requires a synchronous delivery
classification.

Before `sessions.open(rotatedTool)`, apply the same monotonic authority comparison against the current
Session:

- reject with `DeliveryOutcome.UNAVAILABLE` when the current same-Tool Session has a greater
  `lockVersion`;
- at equal lock version, require equal Item Instance ID, Instance Epoch, and Status;
- accept a greater lock version or exact idempotent equality.

Keep the existing immediate Held Authorization invalidation before changing Session or physical item
state. This guard exists so a delayed direct delivery snapshot cannot overwrite a newer revoke or
other higher-version authority.

Do not change `ReissueCoordinator`, `ReissueDeliveryGateway`, transaction semantics, price, or
migration schema in Candidate-7.

### 4.7 Quit and stop

`onQuit(...)` runs on the Bukkit Main Thread and must:

1. advance/invalidate the Player authority generation before any asynchronous checkpoint callback;
2. remove Held Authorization immediately;
3. start the existing checkpoint;
4. schedule the eventual Session close on the Bukkit Main Thread;
5. close only when the quit invalidation generation is still current and the Player is still offline.

A rejoin/new authority request must make the old quit callback stale so it cannot close the newly
opened Session.

`stopAndFlush()` must set `accepting = false` and call
`MainAuthorityOperationSerializer.shutdown()` before clearing Session-adjacent runtime state and
before starting the existing final flush. Clear the generation map after acceptance is disabled.
Any already-running database stage may finish, but its Main Thread application must fail the current
request predicate and perform no Session/inventory/Held Authorization mutation.

## 5. Exact test changes

### 5.1 Tests-first RED

Before Product code changes, add the first two runtime tests below to
`MainAuthorityFailClosedOrderingTest` and run the exact focused command in section 6.1.

The Candidate-6 baseline must fail because:

- two same-Player authority database stages can be pending/running concurrently; and/or
- the older completion can still apply after the newer request has been accepted.

The RED failure must be an ordering/state assertion. Compilation errors, mock setup errors, timeouts,
or unrelated failures do not qualify.

Record the failing test names and assertion output. Then implement the Product design.

### 5.2 New serializer tests

Create `MainAuthorityOperationSerializerTest` with exactly these behavioral tests:

```text
samePlayerOperationsRunFifo
differentPlayersMayRunConcurrently
failedOperationDoesNotPoisonPlayerTail
shutdownRejectsNewAndQueuedOperations
```

Adapt the already proven Frontier serializer test structure. Use bounded latches/timeouts. Do not use
unbounded sleeps or race-probability loops.

### 5.3 Runtime ordering tests

Add or retain these exact behaviors in `MainAuthorityFailClosedOrderingTest`. Method names may differ
only for Java readability; each listed behavior and assertion is mandatory.

```text
samePlayerAuthorityDatabaseStagesAreSerializedInAcceptanceOrder
```

- start request A and hold its database stage;
- accept request B for the same Player;
- assert B's database supplier has not started while A remains incomplete;
- complete A and assert B starts afterward.

```text
newerRequestAcceptanceMakesOlderRefreshCompletionNonApplicable
```

- capture Epoch/lock N in refresh A and hold its completion;
- accept a newer reissue/authority request B so A's generation is stale;
- complete A;
- assert A does not open Session N, reconcile inventory, or restore Held Authorization;
- complete B and assert only B's current authority becomes visible.

```text
supersededReissueDoesNotDeliverOrReauthorize
```

- let reissue A durably return `APPLIED` but hold its runtime completion;
- accept newer revoke/authority request B;
- complete A;
- assert A returns `SUPERSEDED`, starts no delivery/follow-up refresh, and does not expose ACTIVE
  authority;
- complete B and assert the newer authoritative state is installed.

```text
staleRecoveryCannotCloseOrOverwriteNewerSession
```

- hold a conflict/failure recovery result for request A;
- accept/apply newer request B;
- complete A's recovery;
- assert B's Tool ID/Item Instance/Epoch/lock version/Status and Held Authorization remain unchanged.

```text
emptyOlderResultCannotCloseSessionAdvancedAfterRequestBaseline
```

- begin A from baseline lock N;
- advance the Session to lock N+1 through a later authoritative path;
- complete A with empty/unavailable authority;
- assert the N+1 Session remains and A performs no close.

```text
quitObsoletesOutstandingCompletionAndOldQuitCallbackCannotCloseRejoinedSession
```

- hold an authority completion;
- invoke quit and hold checkpoint completion;
- invoke a new join/open request;
- complete the old authority and old quit callback;
- assert neither reopens nor closes the new Session.

```text
stopObsoletesOutstandingCompletion
```

- hold an authority completion;
- invoke `stopAndFlush()`;
- complete the old operation;
- assert no Session open, inventory reconciliation, or Held Authorization restoration occurs.

```text
directPaidDeliveryRejectsOlderLockVersion
```

- load a current Session at lock N+1;
- call `deliverReissued(...)` with the same Tool at lock N;
- assert `UNAVAILABLE`, no Session replacement, and no physical insertion.

Retain all Candidate-6 immediate fail-close, success/no-change/conflict/failure, stale physical item,
Broken Tool branch, ordinary item, Main transition/action, and Frontier tests. Do not weaken or delete
them.

The test fixture may gain deterministic controls to:

- start a queued database supplier without completing its returned future;
- complete a selected pending operation;
- observe supplier start order;
- hold/release Main Thread callbacks;
- count delivery attempts and reconciliation mutations.

These controls are test-only and must not introduce timing-probability tests.

## 6. Exact validation commands

Use the repository wrapper and run from repository root. Stop at the first failure.

### 6.1 RED and focused GREEN

RED before Product changes, then the same command after Product changes:

```bash
./gradlew --no-daemon :plugins:wayfarer-main:test \
  --tests 'io.github.eariver.wayfarer.main.gameplay.MainAuthorityFailClosedOrderingTest'
```

Serializer GREEN:

```bash
./gradlew --no-daemon :plugins:wayfarer-main:test \
  --tests 'io.github.eariver.wayfarer.main.gameplay.MainAuthorityOperationSerializerTest'
```

Combined focused regression:

```bash
./gradlew --no-daemon :plugins:wayfarer-main:test \
  --tests 'io.github.eariver.wayfarer.main.gameplay.MainAuthorityFailClosedOrderingTest' \
  --tests 'io.github.eariver.wayfarer.main.gameplay.MainHeldAuthorizationTransitionTest' \
  --tests 'io.github.eariver.wayfarer.main.gameplay.HeldGrowthToolAuthorizationTest' \
  --tests 'io.github.eariver.wayfarer.main.gameplay.HeldGrowthToolAuthorizerTest'
```

### 6.2 Module and integration validation

```bash
./gradlew --no-daemon :plugins:wayfarer-main:test
./gradlew --no-daemon :plugins:wayfarer-frontier:test
./gradlew --no-daemon \
  :plugins:wayfarer-main:mariaDbIntegrationTest \
  :plugins:wayfarer-frontier:mariaDbIntegrationTest
```

### 6.3 Repository validation

```bash
./gradlew --no-daemon clean check
./gradlew --no-daemon clean assemble
bash scripts/release/test-v002-handoff-mapping.sh
bash scripts/release/test-runtime-jar-manifest.sh
bash scripts/release/test-scoped-runtime-jars.sh
bash scripts/release/test-scoped-stable-package.sh
bash scripts/release/test-release-policy.sh
bash scripts/release/verify-v002-plugin-packaging.sh
git diff --check
git status --short
```

Record the exact command, result, Java identity, Gradle identity, test totals, failures/errors, and
skipped totals. Do not skip, disable, quarantine, or weaken tests to obtain green.

## 7. Commit, CI, fixation, and package

After all local validation passes:

1. confirm the Product/test changed-file set matches section 3;
2. create one clearly identified Candidate-7 Product commit;
3. push normally without force;
4. record exact Candidate-7 Product HEAD;
5. monitor Normal CI and Pre-client Headless Runtime to completion;
6. classify event/head SHA, checkout SHA, PR merge-ref SHA, relation to Product HEAD, and conclusion;
7. perform two independently recorded clean builds from exact Candidate-7 Product HEAD;
8. require byte-identical Main and Frontier outputs before fixation;
9. stage Candidate-7 separately from Candidate-6.

A Product code/resource change after Candidate-7 fixation rejects Candidate-7 and requires
Candidate-8. Metadata/package corrections that do not change fixed Product bytes do not by themselves
require a new candidate.

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

Do not include a complete repository patch, raw logs, tracked-source copies, JARs, worlds, DB/Redis
data, runtime configuration, credentials, secrets, or raw Player identifiers.

Supply the actual ZIP and sidecar bytes for independent review before Server-side Runtime Preflight.

## 8. Required final report

Report exactly:

```text
CANDIDATE-7 PRODUCT HEAD
FINAL PR HEAD
CHANGED PRODUCT/TEST FILES
RED COMMAND / FAILING TESTS / ASSERTION
FOCUSED GREEN COMMANDS / TOTALS
MAIN TEST TOTAL
FRONTIER TEST TOTAL
INTEGRATION RESULT
CHECK / ASSEMBLE / VALIDATORS
CI RUN ID / EVENT HEAD / CHECKOUT SHA / MERGE REF / CONCLUSION
HEADLESS RUN ID / EVENT HEAD / CHECKOUT SHA / MERGE REF / CONCLUSION
FORMAL BUILD 1 ID / MAIN+FRONTIER SIZE+SHA
FORMAL BUILD 2 ID / MAIN+FRONTIER SIZE+SHA
BINARY IDENTITY RESULT
CANDIDATE-7 ARTIFACT PATHS
SUBMISSION ZIP PATH / SIZE / SHA-256
EXTERNAL SIDECAR PATH / SHA-256
WORKTREE/INDEX / LOCAL-ORIGIN-PR RELATION
SERVER-SIDE RUNTIME PREFLIGHT STATE
MINECRAFT CLIENT TEST STATE
FINAL VERDICT
```

## 9. Successful stop state

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
