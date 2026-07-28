# Project Wayfarer Plugin Release Test Report V0.0.1

Status: Pre-client candidate complete; client acceptance and final release identity pending.

## Release identity

- Tested plugin source: `6d25105f516a76cc373e5259fcef9d34de414543`
- Release tag / URL: Pending
- Candidate artifact/version/filename/SHA-256:
  `0.0.1-rc.1` / `wayfarer-core-0.0.1-rc.1.jar` /
  `f36fe57370b4d123b13b5bf328c029c03407338e83e781953db81547de8a334a`
- Config/migration version: `1` / `V003`
- Java/Gradle/Paper: 25 / 9.6.1 / 1.21.11 build 132 (`c5eb079`)
- MariaDB/Redis/Waymark configuration: MariaDB 11.8, Redis 8-alpine; fixture provider in
  automated suites only; concrete provider `BLOCKED` by ADR 0006

## Authority

- Project reference commit: `344eedc738d75954daa43facfeef302944f2963a`
- Requirement snapshot: `docs/requirements/main-server/Project-Wayfarer-V0.1.0/`
- Referenced Project document versions/blob SHAs: `source.md`

## Scope and compliance

- Implemented scope/additions: Core-only alpha.2–alpha.4 foundations and rc.1 headless harness;
  Main/Frontier gameplay excluded
- Project policy and traceability: In progress; global gate remains `BLOCKED`
- Module/dependency and public API boundary: Beta automated boundary/package scans passed
- Commands/permissions: Health runtime observed; transaction handlers automated; remaining player
  observations are in the client plan
- Database schema/transactions/provider/Redis/health/threading: Provider-independent implementation
  and automated suites passed; headless MariaDB/migration/Redis/health/task bridge passed; concrete
  provider blocked

## Build and automated verification

- Commands/results/test counts: 153 unit, 10 MariaDB, and 6 Redis cases; 0 failed, 0 errors,
  0 skipped in the beta measured suite; rc.1 successful workflow had 0 failed and 0 skipped
- Local rc.1 verification: 153 unit tests and `assemble` passed. Full local `check` was
  environment-blocked at Testcontainers initialization because Docker remained stopped; Docker
  was not started, and successful GitHub Actions is the integration authority.
- Unit/MariaDB/Redis/migration/concurrency/idempotency/failure/restart tests: Passed in
  commit-pinned CI; see phase results and traceability
- Main-thread I/O and disable-callback tests: Automated suite passed; Paper probe verified worker
  and main-thread bridge boundaries and clean disable
- API compatibility/class identity: Passed
- Packaging/reproducibility/relocation/license/secret inspection: Passed for the candidate source;
  same-source Core JAR reproducibility and one-runtime-JAR package gate passed
- Gradle Wrapper: version 9.6.1; tracked wrapper JAR SHA-256
  `497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7` and configured
  distribution SHA-256 `9c0f7faeeb306cb14e4279a3e084ca6b596894089a0638e68a07c945a32c9e14`
  matched the official Gradle checksums

## Isolated test server

- Configuration and reproduction procedure:
  `docs/testing/plans/V0.0.1-rc.1.md` and `scripts/runtime/preclient/run-headless-paper.sh`
- Case-by-case expected/actual/evidence:
  `docs/testing/results/V0.0.1-rc.1.md` and
  `docs/testing/evidence/V0.0.1-rc.1-preclient-headless.md`
- Failure injection and restart/disable/reconnect: Migration checksum and MariaDB unavailable
  failed closed; Redis `DOWN`/`UP`, repeated startup, and clean disable passed
- Performance/tick impact: One headless 20 TPS / 13.2 ms observation and Java 25 thread dump;
  client-visible responsiveness pending

## Results and handoff

- Known limitations: `docs/handoff/V0.0.1/known-limitations.md`
- Failed/skipped/not applicable with reasons: Successful rc.1 run 0/0; earlier harness incidents
  and corrected Paper classloader defects are disclosed in the pre-client evidence index
- Open decisions: ADR 0006 concrete Waymark provider, client acceptance, Project placement/
  acceptance, Owner clearance, final release
- Evidence paths/commits: Successful workflow
  [`30317207610`](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30317207610)
  at `6d25105f516a76cc373e5259fcef9d34de414543`; client result remains blank
- Artifact matrix: `docs/handoff/V0.0.1/artifact-matrix.md`
- Project acceptance input: `docs/handoff/V0.0.1/project-acceptance-input.md`
- Project Runtime changed: No

The valid status phrase is `Automated/headless runtime passed`. This report does not claim
`RC passed`, `Runtime test passed`, `CLEARED`, `READY`, or `requirements_cleared=true`.
