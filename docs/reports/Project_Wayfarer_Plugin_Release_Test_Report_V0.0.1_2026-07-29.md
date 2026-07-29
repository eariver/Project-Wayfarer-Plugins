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
- Authoritative final code-bearing preparation CI: GitHub Actions
  [`30455335160`](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30455335160)
  passed at `d16e92cd47267b749803623a3cf1b58850ac8ce4`
- Earlier supporting preparation CI:
  [`30451364006`](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30451364006)
  passed at `d9753420b658a8beb69915980f7994d5b8f3f274`; historical/supporting only
- Earlier supporting preparation gate CI:
  [`30451214126`](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30451214126)
  passed at `8f4b353d8d8a815fd2d7781671250ed180f37294`; historical/supporting only
- Stable Candidate Client Smoke: `PASS`; client-facing regression not observed
- Stable Client Smoke evidence commit: `7d9a74c6d8a14a2d68d0f3b6e9cf48e1e72dcf06`
- Stable Release Package required asset set: `COMPLETE`
- Handoff source model: workflow main HEAD is captured as an immutable tracked-file snapshot
  before checkout of the unchanged Stable Product Source

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

## Stable Candidate Client Smoke

Java Edition 1.21.11 joined the fixed Stable Candidate on Paper 1.21.11 build 132 / Java 25.0.3
with fresh task-only MariaDB 11.8 and Redis 8-alpine. The probe was absent. Movement, repeated
jump, view movement, chat, and a normal wait completed without freeze, kick, watchdog, significant
lag warning, or Wayfarer failure.

Non-OP admin health was denied safely. With temporary OP, client health showed `overall=UP` and
`Waymark: UP`; Console identified `Vault/RedisEconomy`. A nonexistent transaction UUID returned
the sanitized generic operation-failed response without Paper command error, synchronous exception
escape, internal exception text, secret, raw provider object, or provider reference. OP was
removed, same-account reconnect passed, and the final disconnect/stop saved all dimensions and
exited 0.

No debit, refund, reconcile, or other transaction mutation was performed. Full inventory remains
`LIMITED` for the existing external-plugin reasons; the prior EliteMobs shutdown race did not
recur. Stable product source and candidate SHA are unchanged. Release readiness remains `READY`,
traceability remains `CLEARED`, and Project acceptance remains pending.

## Release and handoff

ADR 0008 authorizes direct stable publication after this local acceptance and does not treat
historical `V0.0.1-alpha.1` as approval authority. `release.yml` now accepts a reviewed
`stable_source_commit`, validates main ancestry and evidence, rebuilds that exact source, and
requires the generated stable JAR to equal the committed local candidate SHA before publication.

Plugin-side traceability is `CLEARED` and release readiness is `READY` for publication. These
markers do not claim Project placement, migration execution, Runtime acceptance, Roadmap Order 9,
or set `requirements_cleared=true`. That input remains an explicit Owner authorization for
source-side stable publication after Plugin-side prerequisites are cleared; it is not Project
acceptance. Stable workflow dispatch, Tag/Release verification, and Project handoff remain pending
explicit post-review actions. Project Runtime was unchanged.

The Stable workflow now assembles the required Release/Handoff package from two explicit
provenance authorities:

- Product build source: immutable `49e00e21716c1c13a2dbb170fdad1b19c4275612`;
- Handoff source: the workflow main revision recorded as `HANDOFF_SOURCE_COMMIT`.

Before Product Source checkout, all required Handoff inputs are validated as tracked regular
non-symlink files and copied to a fixed runner snapshot. Package assembly then attaches the
sanitized configuration, command/permission reference, dependency/placement record, third-party
notices, license, Plugin Test Report, known limitations, rollback procedure, artifact inventory,
Project acceptance input, existing evidence/instruction/traceability/readiness files, and a
publication-time Artifact Matrix. `SHA256SUMS.txt` covers every attached asset except itself and
`RELEASE_MANIFEST.md`; the Manifest explains those self-reference exclusions and records each
snapshot source path, Release filename, and SHA-256.
