# Phase 10C-A Candidate-8 Product Remediation Result

Status: PRODUCT_REMEDIATION_PASS.

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

This is the tracked Luna execution result. It is evidence, not independent ChatGPT acceptance,
Owner acceptance, Runtime authorization, requirements_cleared=true, or release authorization.

## Authority and Recovery Gate

Candidate-8 execution authority was read in full from the latest Origin/PR tree:

- docs/handoff/V0.0.2/phase-10c-a-candidate-8-remediation-handoff.md, Revision A,
  present in commit 1a040fe2e5164f6743269e3aee8f8ea726fe3ede.
- docs/release-readiness/V0.0.2/phase-10c-a-candidate-7-independent-product-review.md,
  present in commit f4f59cdcbbc83c9df689754cee06b73da659242c.
- AGENTS.md.

The required Work Order and Design Specification was also read in full before editing.
The Candidate-7 independent review rejected Candidate-7 only for the final physical delivery
gateway being reachable after same-Player supersession. Revision A prescribed the narrow admission
predicate boundary. No substitute design, FIFO serialization, lock, blocking wait, new executor,
lifecycle framework, broad operation matrix, second formal build, evidence ZIP, or sidecar was added.

Recovery Gate:

    Initial Local HEAD       = a2ffc6a48ee5d29c6ceb961bc7a453534b9e797d
    Initial Origin HEAD      = 1a040fe2e5164f6743269e3aee8f8ea726fe3ede
    Initial PR #14 HEAD      = 1a040fe2e5164f6743269e3aee8f8ea726fe3ede
    Branch                  = feature/V0.0.2-main-frontier
    Initial Worktree/index  = clean
    Fast-forward            = PERFORMED
    Final Recovery HEAD      = 1a040fe2e5164f6743269e3aee8f8ea726fe3ede
    Recovery Gate            = PASS

Candidate-7 Product HEAD 980eda20921a5f3ae1f795a2b9a23b92f53ac8e2 and Candidate-7 result commit
a2ffc6a48ee5d29c6ceb961bc7a453534b9e797d are ancestors of the Recovery Origin HEAD. Candidate-6
and Candidate-7 local artifacts remain preserved and separate:

- Candidate-6 Main: 4,691,735 bytes, SHA-256
  1a5505ae2edffc7d594b1192363f24c9e3bbab525bc738e81c6a66603928a7ae.
- Candidate-6 Frontier: 4,713,179 bytes, SHA-256
  dda3ac825ddde024046e9c72c9954cf3af59ceb1e8643aeb44571ea7f9a312b8.
- Candidate-6 submission ZIP: 10,145 bytes, SHA-256
  4337f09565c256a2d2ca2f1bb5983dee41c3055cd66f0f3582a3ef6e11978f2.
- Candidate-6 submission sidecar SHA-256:
  df16dcfebefe054b929f182957ba96e81f7be001c3b62e6a18735e1a600f8eac.
- Candidate-7 Main: 4,695,730 bytes, SHA-256
  3ecb48bf843203ae2a8dac898d113d6cee2fb69a89871402b3dde9e7abd1fbef.
- Candidate-7 Frontier: 4,713,179 bytes, SHA-256
  dda3ac825ddde024046e9c72c9954cf3af59ceb1e8643aeb44571ea7f9a312b8.

## Product fixation

Candidate-8 Product commit:
698c387dfe2e86de8e48ea59a80f35f14c728e2a.

Changed Product/test files, exactly the four files authorized by Revision A:

- plugins/wayfarer-main/src/main/java/io/github/eariver/wayfarer/main/application/GrowthToolDeliveryCoordinator.java
- plugins/wayfarer-main/src/main/java/io/github/eariver/wayfarer/main/gameplay/MainGameplayRuntime.java
- plugins/wayfarer-main/src/test/java/io/github/eariver/wayfarer/main/application/GrowthToolDeliveryCoordinatorTest.java
- plugins/wayfarer-main/src/test/java/io/github/eariver/wayfarer/main/gameplay/MainAuthorityFailClosedOrderingTest.java

The coordinator retains onJoin(UUID), adds the exact admission overload, evaluates admission and
the physical gateway in the same Bukkit Main Thread runnable, returns SUPERSEDED before mark or
audit for a rejected admission, and preserves existing initial/retry behavior. Main admin Reissue
passes ignoredTool -> isCurrent(request) through that boundary. No Core, Frontier, schema,
migration, GrowthTool, ReissueCoordinator, transaction, price, permission, balance, Fixture,
Resource Pack, build logic, workflow, inventory ownership, or Project Runtime change was made.
No Wayfarer_Frontier_EliteMobsMVI module was created.

## Tests-first evidence

The first fixture attempt was discarded: the existing Paper API unit-test environment had no
RegistryAccess implementation and failed before PlayerInventory.addItem. It was not treated as RED.
The authorized deterministic fixture plumbing then used a test-only delivery coordinator gateway
that calls the mocked PlayerInventory.addItem with a mocked ItemStack. No Product semantics changed.

Focused RED command:

    ./gradlew.bat --no-daemon :plugins:wayfarer-main:test --tests "io.github.eariver.wayfarer.main.gameplay.MainAuthorityFailClosedOrderingTest"

The valid RED had 19 tests, 1 failure, 0 errors, and 0 skipped. The failing method was
deliveryStartedThenSupersededBeforeGatewaySkipsPhysicalMutation. The exact assertion failure was
the required physical-mutation assertion: PlayerInventory.addItem was invoked after the newer
request was accepted. This was not a setup error, compilation error, hang, timeout, or race loop.

Focused GREEN command:

    ./gradlew.bat --no-daemon :plugins:wayfarer-main:test --tests "io.github.eariver.wayfarer.main.gameplay.MainAuthorityFailClosedOrderingTest" --tests "io.github.eariver.wayfarer.main.application.GrowthToolDeliveryCoordinatorTest"

Main ordering suite: 19 tests, 0 failures, 0 errors, 0 skipped.
Coordinator suite: 5 tests, 0 failures, 0 errors, 0 skipped.
The new direct contract test proves rejected admission returns SUPERSEDED and invokes neither the
gateway nor markDelivered. Existing Candidate-7 generation, recovery, quit/stop, fail-close,
stale-item, Broken Tool, ordinary-item, Main transition/action, and delivery tests remain.

## Local validation

The Main regression passed:

    ./gradlew.bat --no-daemon :plugins:wayfarer-main:test

Repository validation passed:

    ./gradlew.bat --no-daemon clean check
    ./gradlew.bat --no-daemon clean assemble
    bash scripts/release/verify-v002-plugin-packaging.sh
    git diff --check
    git status --short

clean check completed with 55 actionable tasks, including Main/Frontier check and MariaDB/Redis
integration paths. The validation assemble completed with 67 actionable tasks. The exact artifact
fixation assemble from Candidate-8 Product HEAD completed at:

    START 2026-08-04T09:05:50.5228471+09:00
    END   2026-08-04T09:05:58.8424857+09:00
    EXIT  0

Execution identity: Java 25.0.3 (Oracle), Gradle 9.6.1, Windows 11. The assemble emitted the
existing Javadoc/doclint warnings caused by Japanese source encoding display, but ended with BUILD
SUCCESSFUL. No local Project Runtime, Paper server, Minecraft Client, or server-side preflight was
started.

## CI and Pre-client Headless evidence

Normal CI run 30863949449 completed SUCCESS with job 91851631142. Pre-client Headless run
30863949421 completed SUCCESS with job 91851631056. Both used Product head
698c387dfe2e86de8e48ea59a80f35f14c728e2a and PR merge-ref
1deefb31e716b56a4b48b6c52e19ea12b0a125bdf.

- Normal CI: https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30863949449
- Pre-client Headless: https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30863949421
- Headless artifact: preclient-headless-evidence, ID 8875549420, 58,354 bytes,
  SHA-256 digest a83f6f5f7fb40fecdced872dd54d3c484844733d5cf4dc03dad118808d79bbbf.

## Candidate-8 artifact fixation

Clean-build record:
local-clean-assemble-698c387dfe2e86de8e48ea59a80f35f14c728e2a-2026-08-04T09:05:50+09:00.

| Artifact | Size | SHA-256 |
|---|---:|---|
| wayfarer-main-0.0.2-SNAPSHOT.jar | 4,696,199 | 93c76efe11211b3f52319513ef560ef26bcda5a04155699e7edce16f5269139d |
| wayfarer-frontier-0.0.2-SNAPSHOT.jar | 4,713,179 | dda3ac825ddde024046e9c72c9954cf3af59ceb1e8643aeb44571ea7f9a312b8 |

Candidate-8 local-only staging paths:

- .ai-work/luna-gpt-5.6-v003/candidate/V0.0.2-Client-Candidate-8/artifacts/clean-build-698c387dfe2e86de8e48ea59a80f35f14c728e2a/main/wayfarer-main-0.0.2-SNAPSHOT.jar
- .ai-work/luna-gpt-5.6-v003/candidate/V0.0.2-Client-Candidate-8/artifacts/clean-build-698c387dfe2e86de8e48ea59a80f35f14c728e2a/frontier/wayfarer-frontier-0.0.2-SNAPSHOT.jar

Candidate-6 and Candidate-7 staging roots were not overwritten. No Candidate-8 evidence ZIP or
sidecar was created.

## Tracked result and runtime handoff

- docs/handoff/V0.0.2/phase-10c-a-candidate-8-product-remediation-result.md
- docs/handoff/V0.0.2/phase-10c-a-candidate-8-runtime-handoff.md
- docs/work-orders/V0.0.2/execution-status.md

These are metadata-only records after artifact fixation. They do not alter the fixed Product HEAD
or the validated JAR bytes. The final PR HEAD after this metadata commit is reported with its
immutable SHA in the final report.

## Boundaries, limitations, and authority conflicts

Server-side Runtime Preflight, MariaDB/Redis Project Runtime operations, Paper installation or
restart, world/configuration changes, migration application, Minecraft Client connection, Client
Test scenarios, Project Issue #4, Fixture changes, PR merge/Ready for Review, tag/release, and
requirements_cleared were not performed. No runtime identifiers were created.

There was no authority conflict. The discarded first RED attempt was a deterministic test-environment
RegistryAccess setup problem and was not counted as evidence. The allowed fixture-only adjustment
then produced the prescribed physical-mutation RED. Candidate-8 remains pending independent
ChatGPT review and Owner acceptance.

## Required final report

    CANDIDATE-8 PRODUCT HEAD
    698c387dfe2e86de8e48ea59a80f35f14c728e2a

    FINAL PR HEAD
    Recorded after metadata-only documentation commit and final Git verification.

    CHANGED PRODUCT/TEST FILES
    GrowthToolDeliveryCoordinator.java; MainGameplayRuntime.java; GrowthToolDeliveryCoordinatorTest.java; MainAuthorityFailClosedOrderingTest.java

    RED TEST / ASSERTION
    deliveryStartedThenSupersededBeforeGatewaySkipsPhysicalMutation; 19 tests, 1 physical addItem assertion failure.

    FOCUSED GREEN RESULT
    Main 19 tests and Coordinator 5 tests; all 0 failures, 0 errors, 0 skipped.

    MAIN TEST RESULT
    :plugins:wayfarer-main:test PASS.

    CLEAN CHECK / ASSEMBLE / PACKAGING VALIDATOR
    clean check PASS; clean assemble PASS; verify-v002-plugin-packaging.sh PASS.

    CI RUN ID / HEAD OR MERGE REF / CONCLUSION
    30863949449 / head 698c387dfe2e86de8e48ea59a80f35f14c728e2a / merge ref 1deefb31e716b56a4b48b6c52e19ea12b0a125bdf / SUCCESS.

    HEADLESS RUN ID / HEAD OR MERGE REF / CONCLUSION
    30863949421 / head 698c387dfe2e86de8e48ea59a80f35f14c728e2a / merge ref 1deefb31e716b56a4b48b6c52e19ea12b0a125bdf / SUCCESS.

    CLEAN BUILD ID / MAIN+FRONTIER FILENAME+SIZE+SHA-256
    local-clean-assemble-698c387d... / Main 4696199 / 93c76efe...; Frontier 4713179 / dda3ac825....

    CANDIDATE-8 ARTIFACT PATHS
    .ai-work/luna-gpt-5.6-v003/candidate/V0.0.2-Client-Candidate-8/artifacts/clean-build-698c387dfe2e86de8e48ea59a80f35f14c728e2a/{main,frontier}/

    TRACKED RESULT / RUNTIME HANDOFF PATHS
    phase-10c-a-candidate-8-product-remediation-result.md; phase-10c-a-candidate-8-runtime-handoff.md; docs/work-orders/V0.0.2/execution-status.md

    WORKTREE/INDEX / LOCAL-ORIGIN-PR RELATION
    Final state after metadata-only push; Product HEAD remains 698c387d...; PR #14 OPEN/DRAFT/UNMERGED.

    SERVER-SIDE RUNTIME PREFLIGHT STATE
    NOT STARTED / PENDING INDEPENDENT REVIEW.

    MINECRAFT CLIENT TEST STATE
    NOT STARTED / DEFERRED.

    FINAL VERDICT
    PHASE 10C-A CANDIDATE-8 PRODUCT REMEDIATION: PASS; CANDIDATE-8 FIXED / PENDING INDEPENDENT REVIEW.
