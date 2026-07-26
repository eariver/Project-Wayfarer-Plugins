# Requirement Assessment

## Recommendation

Proceed in gated vertical slices. This preparation change establishes the repository-managed
requirements, release controls, plans, test-result records, and handoff structure. It does not
implement or release `V0.0.1-alpha.1`. Lifecycle implementation starts only after this
preparation pull request is merged.

## Feasibility

The requested `Wayfarer_Core` work is implementable in the existing Gradle multi-module
repository. The module direction and current dependencies can preserve the required boundary:
Main and Frontier may consume Core API, while Core has no dependency on their gameplay domains.
MariaDB can remain the persistent authority, Redis can remain non-authoritative infrastructure,
and Paper mutations can remain on the main thread.

## Current repository gap

- The repository is a scaffold: Core API contracts, initial migrations, sanitized configuration,
  plugin descriptors, and release workflows exist, but production Core services are not yet
  implemented.
- Existing release workflows accept lowercase SemVer, create lowercase `v` tags, and package
  Core, Main, and Frontier together.
- Release manifests do not yet enforce an artifact scope or record config/migration versions and
  artifact hashes together.
- The Core config has no declared config schema version. A release workflow will therefore fail
  closed until the lifecycle slice adds it.
- Automated tests and isolated runtime evidence for the requested Core behavior do not yet exist.
- Release plans, phase test records, and the V0.0.1 handoff package were absent.

## Release automation changes

- Accept human-facing versions and tags with uppercase `V`.
- Strip only the leading `V` for Gradle/plugin internal versions.
- Require `release_scope=core` throughout the first release line.
- Package and attest only `Wayfarer_Core`.
- Record scope, source commit, config version, migration version, and checksums.
- Verify the stable release uses a pre-release whose manifest declares the same scope.
- Preserve `test-server-release` and `main-server-release` environment approvals.

## Authority assessment

No authority conflict was found at Project commit
`344eedc738d75954daa43facfeef302944f2963a`.

- Project Order 9 is Plugin Repository foundation plus `Wayfarer_Core`.
- Project Runtime still records custom Plugins as planned and not installed.
- Waymark remains RedisEconomy `4.5.12-wayfarer.1` through the Vault boundary.
- MVI remains authoritative for normal Frontier player state.
- The EliteMobs–MVI decision has not returned `ADAPTER_REQUIRED`; no conditional adapter or
  contract module is authorized.

## Unresolved matters

- The supported Waymark provider API and its exact thread contract must be verified before the
  transaction slice. No unsafe off-thread provider call will be inferred.
- Exact command permissions and operator confirmation UX are finalized with the slices that
  implement them and with Project Permission Phase 1B.
- Config version `1` is planned for the lifecycle slice but is not asserted by this preparation
  change.
- MariaDB, Redis, Paper, and provider runtime evidence remains pending.

## Stop and escalation conditions

Stop and request an owner decision if implementation requires a forbidden dependency direction,
normal inventory persistence, an external Plugin's internal storage, destructive/applied
migration changes, unsafe main-thread I/O, a breaking public API change, runtime deployment, or
the unapproved EliteMobs–MVI adapter. Use the escalation report format in the received requirement.

## Initial release scope

`V0.0.1` releases only `Wayfarer_Core`. `wayfarer-api`, `wayfarer-common`, and
`wayfarer-testkit` may be developed in support of Core. Main, Frontier, and the LeafGrapple
adapter remain build-only skeletons and are excluded from release assets.

## Test-server assumptions

Runtime tests use a repository-external isolated server with Paper 1.21.11, Java 25, isolated
MariaDB and Redis, and the Project-compatible Waymark provider boundary. Server data, JARs,
secrets, worlds, logs, and caches are never committed. Client-observed behavior is not marked
passed without user evidence.

## Project Runtime boundary

This repository prepares source and handoff evidence only. It does not install a JAR, apply a
migration, render or modify Project runtime configuration, set secrets or permissions, start a
Project server, modify worlds/player data, or update Project `versions.yml` or
`plugin-manifest.yml`.
