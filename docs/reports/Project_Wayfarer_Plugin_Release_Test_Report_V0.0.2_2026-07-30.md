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

## Not run / not claimed

- Canonical LeafGrapple motion: `EXTERNAL_BLOCKED` and `CLIENT_TEST_REQUIRED`.
- Visual/interaction client acceptance: `CLIENT_TEST_REQUIRED`.
- Project Runtime acceptance, Project migration and Project deployment: not authorized.
- Stress, full crash matrix and exhaustive failure timing: not required.

Development `0.0.2-SNAPSHOT` JAR hashes are intentionally not release evidence. No candidate SHA,
tag, pre-release, stable release, merge, or `requirements_cleared=true` assertion was created.
