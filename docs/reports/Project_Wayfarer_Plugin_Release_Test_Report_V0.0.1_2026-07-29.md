# Project Wayfarer Plugin Release Test Report V0.0.1

- Status: Plugin-side stable release preparation complete; publication and Project acceptance
  pending
- Stable source: `49e00e21716c1c13a2dbb170fdad1b19c4275612`
- Stable preparation evidence record: `eabda6d2c83e7369dd9f4ba4725f80d601a51062`
- Version / scope: `0.0.1` / Core only
- Stable candidate: `wayfarer-core-0.0.1.jar`
- SHA-256: `B045581D3984DDDBA10ED7B2ADA435926B8538BA9B29A1151550CE59588395A2`
- Config / migration: `1` / `V003`
- Release tag / URL: Planned `V0.0.1` / pending stable publication
- Pre-release: Not required by ADR 0008; not created
- Preparation CI: GitHub Actions
  [`30451214126`](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30451214126)
  passed at `8f4b353d8d8a815fd2d7781671250ed180f37294`

## Verification

Stable-version `clean check assemble`, 192 unit, 14 MariaDB, 6 Redis, packaging, and two-build
reproducibility passed with zero failed/errors/skipped tests. The local isolated Paper 1.21.11
build 132 server used Java 25.0.3, task-only MariaDB 11.8 and Redis 8-alpine, and the complete
Owner-supplied 23-JAR inventory. Console health returned overall/Transaction/Waymark UP with safe
`Vault/RedisEconomy` identity.

The representative path passed: Vault/Wayfarer 37.5, long debit 25, both 12.5, exact idempotent
replay, insufficient-funds failure, representative direct Vault withdraw/deposit, long refund 25,
and final Vault/Wayfarer 37.5. The first stop and probe-free restart/final stop saved all dimensions
and exited 0. Generated-secret patterns, Wayfarer failure markers, and Wayfarer classloader/API
identity errors were zero.

Full-inventory startup is `LIMITED` because the supplied `VelocityScoreboardAPI` JAR is a library
without a Paper plugin descriptor. Iris warnings and one EliteMobs shutdown task race are also
disclosed. Twenty-four actual plugins initialized; required Wayfarer dependencies and the Core
representative path were unaffected. Details and harness corrections are in
`docs/testing/results/V0.0.1-stable-local-acceptance.md`.

## Release and handoff

ADR 0008 authorizes direct stable publication after this local acceptance and does not treat
historical `V0.0.1-alpha.1` as approval authority. `release.yml` now accepts a reviewed
`stable_source_commit`, validates main ancestry and evidence, rebuilds that exact source, and
requires the generated stable JAR to equal the committed local candidate SHA before publication.

Plugin-side traceability is `CLEARED` and release readiness is `READY` for publication. These
markers do not claim Project placement, migration execution, Runtime acceptance, Roadmap Order 9,
or `requirements_cleared=true`. Stable workflow dispatch, Tag/Release verification, and Project
handoff remain pending explicit post-review actions. Project Runtime was unchanged.
