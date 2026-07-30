# Project Wayfarer Plugin V0.0.2 Release Test Report

Report date: 2026-07-30

Corrected implementation commit: `ddc6711e358067414d180d0780eac490faf00dff`

Release candidate: not fixed

Readiness: `PLUGIN_REVIEW_REQUIRED`

## Passed

- Normal Gradle `check`, including Core Redis/MariaDB, Main MariaDB and Frontier MariaDB
  Testcontainers suites.
- V0.0.1 public API executable compatibility and immutable migration hashes.
- Main/Frontier empty schema, V001→V002, repeated migration and intentionally broken migration
  failure behavior.
- Main repair and Frontier shop idempotency; ambiguous effect becomes `UNKNOWN`, is not
  automatically retried, and a repair whose physical effect may exist is not refunded.
- LeafGrapple missing/version/public-API/capability probes.
- V0.0.x stable/correction grammar, exact release scopes, scoped collection/package/runtime
  manifest, V0.0.1 package compatibility and publication recovery.
- Official Node 24 action-major static gate.
- `assemble` and Main/Frontier shaded-JAR boundary verification.
- Corrected Main and Frontier focused unit tests.
- Core+Main and Core+Frontier same-schema migration histories, repeat-zero behavior, ownership
  boundaries, and JDBC saga CAS integration tests.
- Local Docker-independent `clean check assemble` after the correction: PASS. Testcontainers
  execution is intentionally taken from GitHub CI because no local Docker daemon was available.
- Corrected normal CI:
  [30509795935](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30509795935),
  commit `ddc6711e358067414d180d0780eac490faf00dff`: PASS.
- Corrected isolated Headless Paper:
  [30509795942](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30509795942),
  commit `ddc6711e358067414d180d0780eac490faf00dff`: PASS.

## Failed then fixed

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

## Not run / not claimed

- Canonical LeafGrapple motion: `EXTERNAL_BLOCKED` and `CLIENT_TEST_REQUIRED`.
- Visual/interaction client acceptance: `CLIENT_TEST_REQUIRED`.
- Project Runtime acceptance, Project migration and Project deployment: not authorized.
- Stress, full crash matrix and exhaustive failure timing: not required.

Development `0.0.2-SNAPSHOT` JAR hashes are intentionally not release evidence. No candidate SHA,
tag, pre-release, stable release, merge, or `requirements_cleared=true` assertion was created.
