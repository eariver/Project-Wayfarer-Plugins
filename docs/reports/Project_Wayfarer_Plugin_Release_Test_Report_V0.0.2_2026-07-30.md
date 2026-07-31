# Project Wayfarer Plugin V0.0.2 Release Test Report

Report date: 2026-07-30

Second-review implementation/evidence commit: `2114e3cd8f5d6fcd7b4aeb22fd4343290e297072`

Release candidate: not fixed

Readiness: `PLUGIN_REVIEW_REQUIRED`

## Passed

- Normal Gradle `check`, including Core Redis/MariaDB, Main MariaDB and Frontier MariaDB
  Testcontainers suites.
- V0.0.1 public API executable compatibility and immutable migration hashes.
- Main/Frontier empty schema, prior-version upgrade, repeated migration and intentionally broken
  migration failure behavior. Main V003 physical-instance authority is included.
- Main repair and Frontier shop idempotency; ambiguous effect becomes `UNKNOWN`, is not
  automatically retried, and a repair whose physical effect may exist is not refunded.
- LeafGrapple missing/version/public-API/capability probes.
- V0.0.x stable/correction grammar, exact release scopes, scoped collection/package/runtime
  manifest, V0.0.1 package compatibility and publication recovery.
- Official Node 24 action-major static gate.
- `assemble` and Main/Frontier shaded-JAR boundary verification.
- Main physical-claim/reissue and shared owner-bound container/drag guard tests.
- Main functional repair GUI session, single-use confirmation, evolution-only durability restore,
  and config remaining-ratio tests.
- Frontier permanent-item metadata/guard, unbreakable Elytra, flight-duration-3 rocket,
  navigation action, launchpad identity/replay/policy/active-index/public-break tests.
- Core+Main and Core+Frontier same-schema migration histories, repeat-zero behavior, ownership
  boundaries, and JDBC saga CAS integration tests.
- Local `clean check assemble` after the second-review correction: PASS, including available
  MariaDB and Redis integration tasks. A timed-out wrapper invocation left Windows file locks;
  after stopping only those residual Gradle workers, the complete task graph finished and
  `check assemble --no-build-cache` confirmed all 79 tasks successful/up-to-date.
- Corrected normal CI:
  [30509795935](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30509795935),
  commit `ddc6711e358067414d180d0780eac490faf00dff`: PASS.
- Corrected isolated Headless Paper:
  [30509795942](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30509795942),
  commit `ddc6711e358067414d180d0780eac490faf00dff`: PASS.
- Second-review normal CI:
  [30546252168](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30546252168),
  commit `2114e3cd8f5d6fcd7b4aeb22fd4343290e297072`: PASS.
- Second-review isolated Headless Paper:
  [30546252420](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30546252420),
  commit `2114e3cd8f5d6fcd7b4aeb22fd4343290e297072`: PASS.

## Failed then fixed

- Second-review Headless run
  [30544431524](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30544431524)
  passed all isolated build and startup cases but failed the final history-count assertion because
  the harness still expected Main `V001–V002 + baseline = 3` after additive Main V003 made the
  correct count 4. Run
  [30545124212](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30545124212)
  was cancelled as superseded once the identical stale assertion was known. Commit `c6542ca`
  updates only the Main count; Frontier remains 3.
- Headless run
  [30545484393](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30545484393)
  exposed a test-only self-starvation race: the Redis Pub/Sub probe blocked one Core executor
  thread while Main/Frontier migrations occupied the other, so the handler queued to that same
  executor missed its three-second wait. Commit `2114e3c` runs the blocking reflection probe on
  an independent test background thread and retains a bounded ten-second delivery wait.
- Pre-client Headless run
  [30476250410](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30476250410)
  failed because Vault API was absent from every Paper plugin classloader; Core raised
  `NoClassDefFoundError`, then the hard-dependent probe could not resolve the Core API.
- Runs
  [30506338795](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30506338795)
  and
  [30506338797](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30506338797)
  exposed unordered Core shaded-JAR consumption in module MariaDB tests.
- Headless runs
  [30506691754](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30506691754)
  and
  [30506938089](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30506938089)
  then exposed duplicate shaded metadata rejected by Paper remapping and provider-startup timing.
- Headless run
  [30508550428](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30508550428)
  proved Core itself was ready but Main/Frontier checked the asynchronously published concrete
  capabilities too early. Both modules now use a bounded readiness wait and still fail closed
  when the capabilities never become available.
- Headless run
  [30508837532](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30508837532)
  reached the corrected module path but the harness-only 1000 ms provider startup timeout raced a
  slow hosted main-thread dispatch. The harness now uses the production-default 5000 ms timeout;
  provider-outage behavior remains a separate explicit case.
- Headless run
  [30509207888](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30509207888)
  passed Core provider startup and exposed that module-local Hikari pools relied on JDBC
  ServiceLoader discovery across Paper plugin classloaders. Main and Frontier now explicitly load
  the shaded MariaDB driver, matching the established Core pool boundary.
- Headless run
  [30509460990](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30509460990)
  started both module pools and exposed Flyway scanning with the Core executor thread context
  classloader, which reported zero module migrations. Each module now binds Flyway resource
  discovery to its own plugin classloader.
- Superseded runs
  [30509752581](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30509752581)
  and
  [30509752604](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30509752604)
  were cancelled before conclusion after the preceding raw evidence showed the harness assertion
  used `absent` while the sanitized command contract emits `none`. Commit `ddc6711e` corrected
  only that assertion before the successful final runs.
- The harness now uses a test-only Vault plugin that shades Vault API, registers a representative
  RedisEconomy-named Economy service through ServicesManager, keeps the probe API compile-only,
  rejects duplicate JAR entries and Core/probe enable or classloading errors, and waits only for
  the bounded Core provider-readiness window.
- Review defects fixed in code: typed config was disconnected, progress baselines were missing,
  enchant thresholds used n=0, Silk branch activation was delayed, threshold caching was absent,
  clear repair failures were not terminal, purchase fulfillment stopped at payment, and
  non-launch claims were not released.
- Second-review defects fixed in code: physical item authority was incomplete, owner-bound items
  could enter containers, Main GUI repair was a display stub, durability could be restored outside
  evolution, permanent traversal items and rocket metadata were incomplete, launchpad identity and
  placement revalidation were partial, arbitrary-player removal/protection scope were misaligned,
  and navigation actions were not connected.

## Pre-client Phase 01 — Growth progress Long.MAX_VALUE saturation (2026-07-31)

Scope: PR #14 final pre-client correction Phase 01 only. Positive growth progress
addition saturates at `Long.MAX_VALUE` without negative wrap, and
`EvolutionPlan.evaluate(Long.MAX_VALUE)` terminates because threshold generation now
saturates internally and stops at the `Long.MAX_VALUE` terminal threshold. Balance,
evolution formula, progress units, death, reissue, Frontier, permission and
presentation behavior are unchanged. No migration was added.

- Implementation commit: `85be072a7a3cb8d9d5b130191d08a89b1b347386`
  (`fix(main): saturate cumulative progress at long maximum`).
- Validation HEAD: `85be072a7a3cb8d9d5b130191d08a89b1b347386` (identical to the
  implementation HEAD; no code change was needed during validation).

Initial attempt (2026-07-31, same HEAD):

- `.\gradlew.bat :plugins:wayfarer-main:test --tests "io.github.eariver.wayfarer.main.domain.EvolutionPlanTest" --tests "io.github.eariver.wayfarer.main.domain.GrowthToolProgressTest" --tests "io.github.eariver.wayfarer.main.domain.GrowthToolAuthorityTest" --tests "io.github.eariver.wayfarer.main.application.GrowthSessionStoreTest" --console=plain -q`:
  PASS — 15 focused tests, 0 failures, 0 errors, 0 skipped.
- `.\gradlew.bat :plugins:wayfarer-main:test :plugins:wayfarer-main:compileMariaDbIntegrationTestJava --console=plain`:
  PASS — 40 Main unit tests across 14 classes, 0 failures, 0 errors, 0 skipped;
  MariaDB integration source set compiles.
- `:plugins:wayfarer-main:mariaDbIntegrationTest`: ENVIRONMENT_BLOCKED, not executed.
  The local Docker daemon/Compose services were not running
  (`npipe:////./pipe/dockerDesktopLinuxEngine` unreachable), so the MariaDB
  Testcontainers suite — including the new
  `growthToolCheckpointRoundTripsLongMaxProgress` — could not start. Not claimed.

Re-run after the Docker Compose environment was started (2026-07-31, same HEAD,
image `mariadb:11.8` verified pullable):

- `.\gradlew.bat :plugins:wayfarer-main:cleanTest :plugins:wayfarer-main:test --console=plain`:
  PASS (BUILD SUCCESSFUL); the `test` task outcome was restored FROM-CACHE, so it was
  superseded by the forced local execution below.
- `.\gradlew.bat :plugins:wayfarer-main:cleanTest :plugins:wayfarer-main:test --no-build-cache --console=plain`:
  PASS — forced fresh local execution; 40 tests across 14 classes, 0 failures,
  0 errors, 0 skipped.
- `.\gradlew.bat :plugins:wayfarer-main:mariaDbIntegrationTest --console=plain`:
  PASS — 5 tests, 0 failures, 0 errors, 0 skipped, including
  `growthToolCheckpointRoundTripsLongMaxProgress` (`Long.MAX_VALUE` checkpoint
  save/read round-trip against a MariaDB 11.8 Testcontainers instance).

Code modification during validation: none. Phase 01 initial verdict: **PASS** —
superseded by the post-validation correction recorded below.

Phases 02–08, client gameplay testing and the stable V0.0.2 release remain
incomplete. This entry does not assert `requirements_cleared=true` or V0.0.2
completion.

### Post-validation correction — terminal threshold cache idempotency (2026-07-31)

Post-validation code review found a blocking defect in the Phase 01 implementation:
once the threshold cache ends at the `Long.MAX_VALUE` terminal threshold, a repeated
`thresholdsThrough(Long.MAX_VALUE)` / `evaluate(Long.MAX_VALUE, ...)` call missed the
early return (`last > progress` is false at `MAX == MAX`), re-entered threshold
generation, and appended a duplicate `Long.MAX_VALUE` entry on every call. Because
`upperBound` counts every entry `<= progress`, repeated terminal evaluations inflated
the evolution count and grew the cache without bound — a non-idempotent terminal
evaluation.

- Correction commit: `b4ac6392503646fa5983825b983e0c0552b69e74`
  (`fix(main): keep terminal evolution threshold idempotent`), an additional commit on
  top of the unchanged Phase 01 implementation commit; no amend, no rebase.
- Correction: when the cache tail is already `Long.MAX_VALUE`, the cache is returned
  unchanged, so the terminal threshold is never duplicated. The general early-return
  condition stays `last > progress`, so a non-MAX cached threshold equal to the
  requested progress still triggers generation of the next threshold. Balance,
  threshold formula and progress units are unchanged. No migration was added.
- The initial validation results above are retained unchanged; the initial PASS
  verdict was corrected by this post-validation review.

Added regression coverage
(`EvolutionPlanTest.terminalThresholdAtLongMaxIsIdempotentAcrossRepeatedEvaluations`,
plus a cache-size assertion in
`rejectsInvalidPlansAndSaturatesThresholdsAtLongMax`):

1. Repeated `evaluate(Long.MAX_VALUE)` returns the same evolution count.
2. `cachedThresholdCount()` does not increase across repeated calls.
3. Exactly one `Long.MAX_VALUE` terminal threshold exists in the cache.
4. Repeated `thresholdsThrough(Long.MAX_VALUE)` returns identical content.
5. A non-MAX cached threshold equal to progress still generates the next threshold.

Final test results (validation HEAD `b4ac6392503646fa5983825b983e0c0552b69e74`):

- `.\gradlew.bat :plugins:wayfarer-main:test --tests "io.github.eariver.wayfarer.main.domain.EvolutionPlanTest" --no-daemon --console=plain`:
  PASS — 8 tests, 0 failures, 0 errors, 0 skipped.
- `.\gradlew.bat :plugins:wayfarer-main:cleanTest :plugins:wayfarer-main:test --no-build-cache --no-daemon --console=plain`:
  PASS — 41 tests across 14 classes, 0 failures, 0 errors, 0 skipped (forced fresh
  local execution).

Phase 01 final verdict after correction: **PASS**. Phases 02–08, client gameplay
testing and the stable V0.0.2 release remain incomplete; this correction does not
assert `requirements_cleared=true` or V0.0.2 completion.

## Not run / not claimed

- Pre-client correction Phases 02–08 (Frontier durable death redelivery and safe-entry
  result notification, Main death handling and player-paid reissue, admin permission
  split, owner-decision documentation): not implemented, not claimed.
- Canonical LeafGrapple motion: `EXTERNAL_BLOCKED` and `CLIENT_TEST_REQUIRED`.
- Visual/interaction client acceptance: `CLIENT_TEST_REQUIRED`.
- Project Runtime acceptance, Project migration and Project deployment: not authorized.
- Stress, full crash matrix and exhaustive failure timing: not required.

Development `0.0.2-SNAPSHOT` JAR hashes are intentionally not release evidence. No candidate SHA,
tag, pre-release, stable release, merge, or `requirements_cleared=true` assertion was created.
