# Phase 10C-A Candidate-7 Product Remediation Result

Status: PRODUCT_REMEDIATION_PASS.

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

This is the tracked Luna execution result. It does not constitute independent ChatGPT review,
Owner acceptance, Runtime authorization, requirements_cleared=true, or release authorization.

## Authority and Recovery Gate

The following documents were read in the user-specified order at the initial current PR HEAD
c08134b9e638709ac9588b6700651a090925dc91, before Product editing:

1. docs/handoff/V0.0.2/phase-10c-a-candidate-7-execution-entry.md Revision B.
2. docs/handoff/V0.0.2/phase-10c-a-candidate-7-remediation-handoff.md Revision C.
3. docs/release-readiness/V0.0.2/phase-10c-a-candidate-6-independent-product-package-review.md.
4. AGENTS.md.

The required Work Order and Design Specification was also read in full before editing.
Revision C was the execution authority. Superseded Revision B design elements were not implemented:
no MainAuthorityOperationSerializer, FIFO serializer, serializer-only test, duplicate individual
validation, second formal clean build, Candidate-7 evidence ZIP, or Candidate-7 sidecar.

Recovery Gate result:

    Local HEAD        = c08134b9e638709ac9588b6700651a090925dc91
    Origin HEAD       = c08134b9e638709ac9588b6700651a090925dc91
    PR #14 HEAD       = c08134b9e638709ac9588b6700651a090925dc91
    Branch            = feature/V0.0.2-main-frontier
    Worktree/index    = clean
    PR state          = OPEN / DRAFT / UNMERGED
    Fast-forward      = ALREADY UP TO DATE
    Recovery Gate     = PASS

Candidate-6 Product 2a3f1cc384c397e610aba33c6ffc0f6a29af2987 and its synchronization merge
b0bb5a5f2047a87a5313701d5ae46f825aec16d4 were present as ancestors. Candidate-6 evidence
remains preserved under .ai-work/luna-gpt-5.6-v003/candidate/V0.0.2-Client-Candidate-6/:

- build-1 and build-2 Main JAR: 4,691,735 bytes, SHA-256
  1a5505ae2edffc7d594b1192363f24c9e3bbab525bc738e81c6a66603928a7ae.
- build-1 and build-2 Frontier JAR: 4,713,179 bytes, SHA-256
  dda3ac825ddde024046e9c72c9954cf3af59ceb1e8643aeb44571ea7f9a312b8.
- submission/Candidate-6-V0.0.2-sanitized-submission.zip: 10,145 bytes, SHA-256
  4337f09565c256a2d2ca2f1bb5983dee41c3055cd66f0f3582a3ef6e11978f2.
- submission/Candidate-6-V0.0.2-sanitized-submission.sidecar.txt: SHA-256
  df16dcfebefe054b929f182957ba96e81f7be001c3b62e6a18735e1a600f8eac.

## Product fixation

Candidate-7 Product commit:
980eda20921a5f3ae1f795a2b9a23b92f53ac8e2.

Changed Product/test files, limited to the Revision C scope:

- plugins/wayfarer-main/src/main/java/io/github/eariver/wayfarer/main/gameplay/MainGameplayRuntime.java
- plugins/wayfarer-main/src/test/java/io/github/eariver/wayfarer/main/gameplay/MainAuthorityFailClosedOrderingTest.java

The implementation uses a per-Player monotonic request generation. It invalidates Held Authorization
on the Bukkit Main Thread before dispatch, rejects stale completion as SUPERSEDED, guards all
runtime mutation and delivery, preserves durable APPLIED audit behavior, applies the specified
monotonic Session lockVersion/authority-field guard, and obsoletes outstanding work on quit and
stop. No Core, Frontier, migration, fixture, configuration, inventory schema, or Project Runtime
change was made. No Wayfarer_Frontier_EliteMobsMVI module was created.

## Tests-first evidence

Focused RED was run before the Product change with:

    ./gradlew.bat --no-daemon :plugins:wayfarer-main:test --tests "io.github.eariver.wayfarer.main.gameplay.MainAuthorityFailClosedOrderingTest"

The Candidate-6 implementation failed the required newerRequestSupersedesOlderRefreshCompletion
state assertion: the expected latest authority remained unrecovered while the older refresh
completion restored the old authority. The run had 13 tests and 1 assertion failure; it was not a
compilation, fixture-setup, timeout, or probabilistic-race failure.

Focused GREEN used the same command after the Product change: 18 tests, 0 failures, 0 errors,
0 skipped. The required behaviors covered newer-refresh supersession, superseded durable mutation
with no runtime apply/delivery, latest recovery, stale recovery protection, and separate quit and
stop obsolescence tests. Existing Candidate-6 fail-close, success/no-change/conflict/failure,
stale physical item, Broken Tool, ordinary item, Main transition/action, and Frontier coverage
remained present.

## Local validation

The full Main regression passed:

    ./gradlew.bat --no-daemon :plugins:wayfarer-main:test

Repository validation passed:

    ./gradlew.bat --no-daemon clean check
    ./gradlew.bat --no-daemon clean assemble
    bash scripts/release/verify-v002-plugin-packaging.sh
    git diff --check

clean check completed with 55 actionable tasks and reached the Main/Frontier check paths and
their MariaDB/Redis integration tasks. The exact Candidate-7 artifact build was one recorded clean
assemble from the fixed Product commit; a second formal clean build was intentionally not run.

Execution identity was Java 25.0.3 (Oracle), Gradle 9.6.1, Windows 11. The local Core diagnostic
test was also isolated and passed after the first Normal CI attempt exposed a transient failure in
that unchanged Core test; no Core source was changed.

## CI and Pre-client Headless evidence

Normal CI run 30831784629 completed successfully on attempt 2 after rerunning only the failed
jobs from attempt 1. Attempt 1 failed only at the unchanged Core test
ManagedExecutorTest.diagnosticFailureDoesNotHideShutdownResult; attempt 2 passed all jobs.
The successful job was 91748340525.

Pre-client Headless run 30831782928 completed successfully; its job was 91747247358.
The head SHA for both workflows was Candidate-7 Product commit
980eda20921a5f3ae1f795a2b9a23b92f53ac8e2. The PR workflow checkout merge ref for both was
b2bbfdd9389dc21486dd636de4b2e15269ca2412, with the Product commit as the PR-head parent.

- Normal CI: https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30831784629
- Pre-client Headless: https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30831782928
- Headless artifact preclient-headless-evidence, ID 8863439623, SHA-256 digest
  4a54053a6f5a1578741364affdbbc6a93fe6c829ae6af1a264dd06b3a2bdf2ee.

## Candidate-7 clean artifact fixation

Clean-build record:
local-clean-assemble-980eda20921a5f3ae1f795a2b9a23b92f53ac8e2-2026-08-04T01:31:03+09:00.
Command: ./gradlew.bat --no-daemon clean assemble. The command ended successfully at
2026-08-04T01:31:09.0097491+09:00.

| Artifact | Size | SHA-256 |
|---|---:|---|
| wayfarer-main-0.0.2-SNAPSHOT.jar | 4,695,730 | 3ecb48bf843203ae2a8dac898d113d6cee2fb69a89871402b3dde9e7abd1fbef |
| wayfarer-frontier-0.0.2-SNAPSHOT.jar | 4,713,179 | dda3ac825ddde024046e9c72c9954cf3af59ceb1e8643aeb44571ea7f9a312b8 |

The separate local-only staging paths are:

- .ai-work/luna-gpt-5.6-v003/candidate/V0.0.2-Client-Candidate-7/artifacts/clean-build-980eda20921a5f3ae1f795a2b9a23b92f53ac8e2/main/wayfarer-main-0.0.2-SNAPSHOT.jar
- .ai-work/luna-gpt-5.6-v003/candidate/V0.0.2-Client-Candidate-7/artifacts/clean-build-980eda20921a5f3ae1f795a2b9a23b92f53ac8e2/frontier/wayfarer-frontier-0.0.2-SNAPSHOT.jar

These are not tracked. No Candidate-7 evidence ZIP or sidecar was created.

## Tracked result and runtime handoff

- docs/handoff/V0.0.2/phase-10c-a-candidate-7-product-remediation-result.md
- docs/handoff/V0.0.2/phase-10c-a-candidate-7-runtime-handoff.md
- docs/work-orders/V0.0.2/execution-status.md

The metadata-only documentation commit that records these files does not alter Candidate-7
Product HEAD or the validated Product/artifact bytes. The final PR HEAD after that documentation
push is reported separately in the final report with its immutable SHA.

## Boundaries, limitations, and authority conflicts

Server-side Runtime Preflight, MariaDB/Redis Project Runtime operations, Paper installation or
restart, Minecraft Client connection, Client Test scenarios, Project Issue #4, fixture changes,
migration application, runtime configuration, PR merge/Ready for Review, tag/release, and
requirements_cleared were not performed. No runtime identifiers were created.

The final Product verdict remains subject to independent ChatGPT review and Owner acceptance.
There was no authority conflict. The only failed validation attempt was the transient unchanged
Core test on Normal CI attempt 1; its allowed failed-job rerun passed and no substitute design was
introduced.

## Required final report

    CANDIDATE-7 PRODUCT HEAD
    980eda20921a5f3ae1f795a2b9a23b92f53ac8e2

    FINAL PR HEAD
    Recorded after the metadata-only documentation commit and reported with the final Git state.

    CHANGED PRODUCT/TEST FILES
    MainGameplayRuntime.java; MainAuthorityFailClosedOrderingTest.java

    RED TEST / ASSERTION
    newerRequestSupersedesOlderRefreshCompletion; 13 tests, 1 state assertion failure on Candidate-6.

    FOCUSED GREEN RESULT
    18 tests, 0 failures, 0 errors, 0 skipped.

    MAIN TEST RESULT
    :plugins:wayfarer-main:test PASS.

    CLEAN CHECK / ASSEMBLE / PACKAGING VALIDATOR
    clean check PASS; clean assemble PASS; verify-v002-plugin-packaging.sh PASS.

    CI RUN ID / CHECKOUT SHA OR MERGE REF / CONCLUSION
    30831784629 / head 980eda20921a5f3ae1f795a2b9a23b92f53ac8e2 / merge ref b2bbfdd9389dc21486dd636de4b2e15269ca2412 / SUCCESS.

    HEADLESS RUN ID / CHECKOUT SHA OR MERGE REF / CONCLUSION
    30831782928 / head 980eda20921a5f3ae1f795a2b9a23b92f53ac8e2 / merge ref b2bbfdd9389dc21486dd636de4b2e15269ca2412 / SUCCESS.

    CLEAN BUILD ID / MAIN+FRONTIER FILENAME+SIZE+SHA-256
    local-clean-assemble-980eda2... / Main 4695730 / 3ecb48...; Frontier 4713179 / dda3ac....

    CANDIDATE-7 ARTIFACT PATHS
    .ai-work/luna-gpt-5.6-v003/candidate/V0.0.2-Client-Candidate-7/artifacts/clean-build-980eda20921a5f3ae1f795a2b9a23b92f53ac8e2/{main,frontier}/

    TRACKED RESULT / RUNTIME HANDOFF PATHS
    phase-10c-a-candidate-7-product-remediation-result.md; phase-10c-a-candidate-7-runtime-handoff.md; docs/work-orders/V0.0.2/execution-status.md

    WORKTREE/INDEX / LOCAL-ORIGIN-PR RELATION
    Final state recorded after metadata-only push; Product HEAD remains 980eda...; PR #14 OPEN/DRAFT/UNMERGED.

    SERVER-SIDE RUNTIME PREFLIGHT STATE
    NOT STARTED / PENDING INDEPENDENT REVIEW.

    MINECRAFT CLIENT TEST STATE
    NOT STARTED / DEFERRED.

    FINAL VERDICT
    PHASE 10C-A CANDIDATE-7 PRODUCT REMEDIATION: PASS; CANDIDATE-7 FIXED / PENDING INDEPENDENT REVIEW.
