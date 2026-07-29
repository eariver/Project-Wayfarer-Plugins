# Project Wayfarer Plugin V0.0.2 Release Test Report

Report date: 2026-07-30

Implementation commit: `981e425a4af619340b64b2060c0cb9ac7219cdd2`

Release candidate: not fixed

Readiness: `PLUGIN_REVIEW_REQUIRED`

## Passed

- Normal Gradle `check`, including Core Redis/MariaDB, Main MariaDB and Frontier MariaDB
  Testcontainers suites.
- 59 XML suites / 260 tests / 0 failures / 0 errors / 0 skipped in the terminal `clean check`.
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

## Not run / not claimed

- Main and Frontier production Paper wiring/headless gameplay:
  `PLUGIN_REVIEW_REQUIRED` by ADR 0009 and B-004.
- Canonical LeafGrapple motion: `EXTERNAL_BLOCKED` and `CLIENT_TEST_REQUIRED`.
- Visual/interaction client acceptance: `CLIENT_TEST_REQUIRED`.
- Project Runtime acceptance, Project migration and Project deployment: not authorized.
- Stress, full crash matrix and exhaustive failure timing: not required.

Development `0.0.2-SNAPSHOT` JAR hashes are intentionally not release evidence. No candidate SHA,
tag, pre-release, stable release, merge, or `requirements_cleared=true` assertion was created.
